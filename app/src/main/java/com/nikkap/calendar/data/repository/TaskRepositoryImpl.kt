package com.nikkap.calendar.data.repository

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.syncEntities
import com.nikkap.calendar.core.utils.toIsoDateWithoutSeconds
import com.nikkap.calendar.data.local.dao.TaskDao
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.TaskListEntity
import com.nikkap.calendar.data.mapper.changePendingAction
import com.nikkap.calendar.data.mapper.markAsSynchronized
import com.nikkap.calendar.data.mapper.toSubtask
import com.nikkap.calendar.data.mapper.toSubtaskEntity
import com.nikkap.calendar.data.mapper.toTask
import com.nikkap.calendar.data.mapper.toTaskDto
import com.nikkap.calendar.data.mapper.toTaskEntity
import com.nikkap.calendar.data.mapper.toTaskList
import com.nikkap.calendar.data.mapper.toTaskListDto
import com.nikkap.calendar.data.mapper.toTaskListEntity
import com.nikkap.calendar.data.remote.api.TasksApi
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.data.remote.dto.TaskListDto
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.model.TaskList
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Collections.emptyList

class TaskRepositoryImpl(
    private val api: TasksApi,
    private val dao: TaskDao,
    private val userPrefRepository: UserPreferencesRepository
) : TaskRepository {
    override val allTasks: Flow<List<Task>> = dao.getNonDeleteTasks()
        .map { entities ->
            entities.map { it.toTask() }
        }
    override val allSubtasks: Flow<List<Subtask>> = dao.getNonDeleteSubtasks()
        .map { entities ->
            entities.map { it.toSubtask() }
        }
    override val allTaskLists: Flow<List<TaskList>> = dao.getNonDeleteTaskLists()
        .map { entities ->
            entities.map { it.toTaskList() }
        }

    override suspend fun getTask(id: String): Task {
        return dao.getTask(id).toTask()
    }

    override suspend fun saveTask(task: Task) {
        dao.insertTask(task.toTaskEntity().changePendingAction(PendingActions.INSERT))
        api.createTask(taskDto = task.toTaskDto())
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task.toTaskEntity().changePendingAction(PendingActions.UPDATE))
        api.updateTask(
            task = task.toTaskDto(),
            taskId = task.id!!
        )
    }

    private suspend fun getRemoteTaskLists(lastSyncTime: String?): Result<List<TaskListDto>?> {
        val taskLists = api.getTaskLists(lastSyncTime)
        if (taskLists.isSuccessful) {
            val entities = taskLists.body()?.items
            return Result.success(entities)
        } else return Result.failure(Exception("Failed to sync tasks with ${taskLists.code()} code"))
    }

    override suspend fun syncAllTasks(): Result<Unit> = try {
        val tasksSyncTime = userPrefRepository.taskSyncTime.first()?.toIsoDateWithoutSeconds()
        val listsPendingResult = tasklistsPendingSync()

        if (listsPendingResult.isFailure) {
            return listsPendingResult
        }

        val tasksPendingResult = tasksPendingSync()

        if (tasksPendingResult.isFailure) {
            return tasksPendingResult
        }

        subtasksPendingSync()

        val remoteTaskLists = getRemoteTaskLists(lastSyncTime = tasksSyncTime)

        if (remoteTaskLists.isFailure) {
            return Result.failure(Exception("Failed to sync task lists"))
        }

        val taskLists: List<TaskListEntity> = remoteTaskLists.getOrNull()?.map {
            if (!it.deleted) it.toTaskListEntity() else it.toTaskListEntity().changePendingAction(
                PendingActions.DELETE
            )
        } ?: emptyList()

        if (taskLists.isEmpty()) {
            return Result.success(Unit)
        }

        val taskListsResult = syncEntities(
            taskLists,
            getLocalEntities = { dao.getNonDeleteTaskLists().first() },
            deleteEntitiesByIds = { dao.deleteTaskListsByIds(it) },
            insertEntities = { dao.insertTaskLists(it) }
        )

        if (taskListsResult.isFailure) {
            return Result.failure(taskListsResult.exceptionOrNull()!!)
        }

        val tasksAndIdsResult =
            getTasksWithTaskListIds(remoteTaskLists = taskLists, tasksSyncTime)

        if (tasksAndIdsResult.isFailure) {
            return Result.failure(tasksAndIdsResult.exceptionOrNull()!!)
        }

        val tasksAndListIds = tasksAndIdsResult.getOrNull()!!

        if (tasksAndListIds.isEmpty()) {
            return Result.success(Unit)
        }

        val taskEntitiesToSync = tasksAndListIds.flatMap { (taskListId, tasks) ->
            val (rootTasks, _) = tasks.partition { it.parent == null }
            rootTasks.map {
                if (!it.deleted) it.toTaskEntity(taskListId) else it.toTaskEntity(
                    taskListId
                ).changePendingAction(PendingActions.DELETE)
            }
        }

        val subtaskEntitiesToSync = tasksAndListIds.flatMap { (taskListId, tasks) ->
            val (_, subtasks) = tasks.partition { it.parent == null }
            subtasks.map {
                if (!it.deleted) it.toSubtaskEntity(taskListId) else it.toSubtaskEntity(
                    taskListId
                ).changePendingAction(PendingActions.DELETE)
            }
        }

        coroutineScope {
            val tasksResult = async {
                syncEntities(
                    taskEntitiesToSync,
                    { dao.getNonDeleteTasks().first() },
                    { dao.deleteTasksByIds(it) },
                    { dao.insertTasks(it) }
                )
            }

            val subtasksResult = async {
                syncEntities(
                    subtaskEntitiesToSync,
                    { dao.getNonDeleteSubtasks().first() },
                    { dao.deleteSubtasksByIds(it) },
                    { dao.insertSubtasks(it) }
                )
            }

            if (tasksResult.await().isSuccess && subtasksResult.await().isSuccess) {
                userPrefRepository.updateTaskSyncTime()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync tasks"))
            }
        }


    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun tasksPendingSync(): Result<Unit> = coroutineScope {
        val pendingEntities = dao.getPendingTasks().first()
        val results = pendingEntities.map { entity ->
            async {
                try {
                    when (entity.pendingAction) {
                        PendingActions.DELETE -> {
                            if (api.deleteTask(taskId = entity.id).isSuccessful) {
                                dao.deleteTask(entity)
                                true
                            } else false
                        }

                        PendingActions.UPDATE -> {
                            val result = api.updateTask(
                                taskId = entity.id,
                                taskListId = entity.taskListId,
                                task = entity.toTaskDto()
                            )
                            if (result.isSuccessful
                            ) {
                                dao.updateTask(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                                true
                            } else false
                        }

                        PendingActions.INSERT -> {
                            val result = api.createTask(
                                taskDto = entity.toTaskDto()
                            )
                            if (result.isSuccessful
                            ) {
                                dao.insertTask(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                                true
                            } else false
                        }

                        PendingActions.NONE -> {
                            true
                        }
                    }
                } catch (_: Exception) {
                    false
                }
            }
        }.awaitAll()

        if (results.all { it }) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Some items failed to sync"))
        }
    }

    private suspend fun subtasksPendingSync() {
        val pendingEntities = dao.getPendingSubtasks().first()
        coroutineScope {
            pendingEntities.map { entity ->
                async {
                    when (entity.pendingAction) {
                        PendingActions.DELETE -> {
                            if (api.deleteTask(taskId = entity.id).isSuccessful) dao.deleteSubtask(
                                entity
                            )
                        }

                        PendingActions.UPDATE -> {
                            val result = api.updateSubtask(
                                subtaskId = entity.id,
                                taskListId = entity.taskListId,
                                subtask = entity.toTaskDto()
                            )
                            if (result.isSuccessful) {
                                dao.updateSubtask(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                            }
                        }

                        PendingActions.INSERT -> {
                            val result = api.createSubtask(
                                taskListId = entity.taskListId,
                                subtask = entity.toTaskDto()
                            )
                            if (result.isSuccessful
                            ) {
                                dao.insertSubtask(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                            }
                        }

                        PendingActions.NONE -> {}
                    }
                }
            }
        }.awaitAll()
    }

    private suspend fun tasklistsPendingSync(): Result<Unit> = coroutineScope {
        val pendingEntities = dao.getPendingTaskLists().first()
        val results = pendingEntities.map { entity ->
            async {
                try {
                    when (entity.pendingAction) {
                        PendingActions.DELETE -> {
                            if (api.deleteTaskList(entity.id).isSuccessful) {
                                dao.deleteTasklist(entity)
                                true
                            } else false
                        }

                        PendingActions.UPDATE -> {
                            val result = api.updateTaskList(
                                taskListId = entity.id,
                                taskList = entity.toTaskListDto()
                            )
                            if (result.isSuccessful) {
                                dao.updateTasklist(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                                true
                            } else false
                        }

                        PendingActions.INSERT -> {
                            val result =
                                api.createTaskList(taskList = entity.toTaskListDto())
                            if (result.isSuccessful) {
                                dao.insertTaskList(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                                true
                            } else false
                        }

                        PendingActions.NONE -> {
                            true
                        }
                    }
                } catch (_: Exception) {
                    false
                }
            }
        }.awaitAll()

        if (results.all { it }) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Some items failed to sync"))
        }
    }

    private suspend fun getTasksWithTaskListIds(
        remoteTaskLists: List<TaskListEntity>,
        lastSyncTime: String?
    ): Result<List<Pair<String, List<TaskDto>>>> = try {

        coroutineScope {
            val taskLists = remoteTaskLists.map { it.toTaskList() }

            val deferredResults = taskLists.map { taskList ->
                async<Pair<String, List<TaskDto>>> {
                    val response = api.getTasks(
                        taskListId = taskList.id,
                        updatedMin = lastSyncTime
                    )

                    if (response.isSuccessful) {
                        taskList.id to (response.body()?.items ?: emptyList())
                    } else {
                        throw Exception("API Error ${response.code()}: Failed to fetch tasks for list ${taskList.id}")
                    }
                }
            }
            val results = deferredResults.awaitAll()

            Result.success(results)
        }

    } catch (e: Exception) {
        Result.failure(e)
    }

}