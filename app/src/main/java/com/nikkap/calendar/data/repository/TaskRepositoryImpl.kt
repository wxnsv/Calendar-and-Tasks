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
import com.nikkap.calendar.data.mapper.toTaskListUpdateDto
import com.nikkap.calendar.data.mapper.toTaskUpdateDto
import com.nikkap.calendar.data.remote.api.TasksApi
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.data.remote.dto.TaskListDto
import com.nikkap.calendar.data.remote.dto.update.TaskUpdateDto
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
import kotlinx.coroutines.launch
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
        api.createTask(
            taskDto = task.toTaskDto(),
            taskListId = task.taskListId.ifBlank { "@default" }
        )
        //TODO // FIX SET TASKLIST
    }

    override suspend fun updateTask(task: Task) {
        val entity = dao.getTask(task.id!!)
        dao.updateTask(task.toTaskEntity().changePendingAction(PendingActions.UPDATE))
        val result = api.updateTask(
            task = task.toTaskUpdateDto(),
            taskListId = entity.taskListId.ifBlank { "@default" },
            taskId = task.id
        )
        if (result.isSuccessful) dao.updateTask(
            result.body()!!.toTaskEntity(task.taskListId)
                .markAsSynchronized(parseIsoDate(result.body()!!.updated))
        ) else handleErrorCode(result.code())
    }

    override suspend fun updateSubtask(subtask: Subtask) {
        dao.updateSubtask(subtask.toSubtaskEntity().changePendingAction(PendingActions.UPDATE))
        val entity = dao.getSubtask(subtask.id)
        val result = api.updateSubtask(
            taskListId = entity.taskListId,
            subtaskId = subtask.id,
            subtask = subtask.toTaskUpdateDto(),
        )
        if (result.isSuccessful) dao.updateSubtask(
            result.body()!!.toSubtaskEntity(entity.taskListId)
                .markAsSynchronized(parseIsoDate(result.body()!!.updated))
        ) else handleErrorCode(result.code())
    }

    override suspend fun updateTaskList(taskList: TaskList) {
        dao.updateTasklist(taskList.toTaskListEntity().changePendingAction(PendingActions.UPDATE))
        val result = api.updateTaskList(
            taskListId = taskList.id,
            taskList = taskList.toTaskListUpdateDto(),
        )
        if (result.isSuccessful) dao.updateTasklist(
            taskList.toTaskListEntity().markAsSynchronized(parseIsoDate(result.body()!!.updated))
        ) else handleErrorCode(result.code())
    }

    override suspend fun deleteTask(id: String) {
        val entity = dao.getTask(id)
        dao.markAsDeleteTask(id, System.currentTimeMillis())
        dao.markAsDeleteSubtasksOfTask(id, System.currentTimeMillis())
        val resultTask = api.deleteTask(taskId = entity.id, taskListId = entity.taskListId)
        val resultSubtasks = deleteAllSubtasks(
            taskListId = entity.taskListId,
            entity.id
        )
        if (resultTask.isSuccessful && resultSubtasks.isSuccess) {
            dao.deleteTask(entity.id)
            dao.deleteSubtasksOfTask(entity.id)
        } else {
            handleErrorCode(resultTask.code())
        }
    }

    /**
     * Deletes all subtask of parent Task on remote
     */
    private suspend fun deleteAllSubtasks(taskListId: String, parentTaskId: String): Result<Unit> {
        return try {
            var countOfSuccess = 0
            val subtaskIdsToDelete = dao.getAllSubtasks()
                .filter { it.parentId == parentTaskId }
                .map { it.id }
            dao.markAsDeleteSubtasksOfTask(parentTaskId, System.currentTimeMillis())
            coroutineScope {
                subtaskIdsToDelete.forEach { subtaskId ->
                    launch {
                        val result = api.deleteSubtask(taskListId, subtaskId)
                        if (result.isSuccessful) countOfSuccess++ else handleErrorCode(result.code())
                    }
                }
            }
            if (countOfSuccess == subtaskIdsToDelete.size) Result.success(Unit)
            else Result.failure(
                Exception("Not all subtasks delete correctly")
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    /**
     * Mark all subtask of parent Task completed on remote
     */

    private suspend fun completeAllSubtasks(
        taskListId: String,
        parentTaskId: String
    ): Result<Unit> {
        return try {
            dao.markAsCompleteSubtasksOfTask(parentTaskId, System.currentTimeMillis())
            var countOfSuccess = 0
            val subtaskIdsToUpdate = dao.getAllSubtasks()
                .filter { it.parentId == parentTaskId }
                .map { it.id }
            coroutineScope {
                subtaskIdsToUpdate.forEach { subtaskId ->
                    launch {
                        val result = api.updateSubtask(
                            taskListId,
                            subtaskId,
                            TaskUpdateDto(id = subtaskId, status = "completed")
                        )
                        if (result.isSuccessful) countOfSuccess++ else handleErrorCode(result.code())
                    }
                }
            }
            if (countOfSuccess == subtaskIdsToUpdate.size) Result.success(Unit)
            else Result.failure(
                Exception("Not all subtasks complete correctly")
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun deleteSubtask(id: String) {
        val subtask = dao.getSubtask(id)
        dao.markAsDeleteSubtask(id, System.currentTimeMillis())
        val result = api.deleteSubtask(subtask.taskListId, subtaskId = id)
        if (result.isSuccessful) {
            dao.deleteSubtask(id)
        } else handleErrorCode(result.code())
    }

    override suspend fun completeSubtask(id: String) {
        dao.completeSubtask(id, System.currentTimeMillis())
        val subtask = dao.getSubtask(id)
        val result = api.updateSubtask(
            subtaskId = id,
            taskListId = subtask.taskListId,
            subtask = TaskUpdateDto(id = id, status = "completed")
        )
        if (result.isSuccessful) {
            val subtask = dao.getSubtask(id)
            dao.updateSubtask(
                subtask.markAsSynchronized(parseIsoDate(result.body()?.updated))
            )
        } else handleErrorCode(result.code())
    }

    override suspend fun completeTask(id: String) {
        dao.completeTask(
            id, System.currentTimeMillis()
        )
        val task = dao.getTask(id)
        completeAllSubtasks(task.taskListId, id)
        val result =
            api.updateTask(
                taskId = id,
                task = TaskUpdateDto(id = id, status = "completed"),
                taskListId = task.taskListId
            )
        if (result.isSuccessful) {
            val task = dao.getTask(id)
            dao.updateTask(
                task.markAsSynchronized(parseIsoDate(result.body()!!.updated))
            )
        } else handleErrorCode(result.code())
    }


    private suspend fun getRemoteTaskLists(lastSyncTime: String?): Result<List<TaskListDto>?> {
        val taskLists = api.getTaskLists(lastSyncTime)
        if (taskLists.isSuccessful) {
            val entities = taskLists.body()?.items
            return Result.success(entities)
        } else {
            handleErrorCode(taskLists.code())
            return Result.failure(Exception("Failed to sync tasks with ${taskLists.code()} code"))
        }
    }

    override suspend fun syncAllTasks(): Result<Unit> = try {
        val tasksSyncTime = userPrefRepository.taskSyncTime.first()?.toIsoDateWithoutSeconds()
        val listsPendingResult = tasklistsPendingSync()

        if (listsPendingResult.isFailure) {
            return listsPendingResult
        }

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

        val tasksAndTaskListIdsResult =
            getTasksWithTaskListIds(remoteTaskLists = taskLists, tasksSyncTime)

        if (tasksAndTaskListIdsResult.isFailure) {
            return Result.failure(tasksAndTaskListIdsResult.exceptionOrNull()!!)
        }

        val tasksAndListIds = tasksAndTaskListIdsResult.getOrNull()!!

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
                pendingSync()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync tasks"))
            }
        }


    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun pendingSync() {
        tasksPendingSync()
        subtasksPendingSync()
    }

    private suspend fun handleErrorCode(code: Int) {
        when (code) {
            in 1..999 -> userPrefRepository.clearLastTaskSyncTime()

            // TODO

        }
    }

    private suspend fun tasksPendingSync(): Result<Unit> = coroutineScope {
        val pendingEntities = dao.getPendingTasks().first()
        val results = pendingEntities.map { entity ->
            async {
                try {
                    when (entity.pendingAction) {
                        PendingActions.DELETE -> {
                            val resultTask =
                                api.deleteTask(
                                    taskId = entity.id,
                                    taskListId = entity.taskListId
                                )
                            val resultSubtasks = deleteAllSubtasks(
                                taskListId = entity.taskListId,
                                entity.id
                            ).isSuccess
                            if (resultTask.isSuccessful && resultSubtasks) {
                                dao.deleteTask(entity.id)
                                dao.deleteSubtasksOfTask(entity.id)
                                true
                            } else {
                                handleErrorCode(resultTask.code())
                                false
                            }
                        }

                        PendingActions.UPDATE -> {
                            val result = api.updateTask(
                                taskId = entity.id,
                                taskListId = entity.taskListId,
                                task = entity.toTaskUpdateDto()
                            )
                            if (result.isSuccessful) {
                                dao.updateTask(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                                true
                            } else {
                                handleErrorCode(result.code())
                                false
                            }
                        }

                        PendingActions.INSERT -> {
                            val result = api.createTask(
                                taskDto = entity.toTaskDto(),
                                taskListId = entity.taskListId
                            )
                            if (result.isSuccessful
                            ) {
                                dao.insertTask(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                                true
                            } else {
                                handleErrorCode(result.code())
                                false
                            }
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
                            val result = api.deleteSubtask(
                                subtaskId = entity.id,
                                taskListId = entity.taskListId
                            )
                            if (result.isSuccessful) {
                                dao.deleteSubtask(entity.id)
                            } else handleErrorCode(result.code())
                        }

                        PendingActions.UPDATE -> {
                            val result = api.updateSubtask(
                                subtaskId = entity.id,
                                taskListId = entity.taskListId,
                                subtask = entity.toTaskUpdateDto()
                            )
                            if (result.isSuccessful) {
                                dao.updateSubtask(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                            } else handleErrorCode(result.code())
                        }

                        PendingActions.INSERT -> {
                            val result = api.createSubtask(
                                taskListId = entity.taskListId,
                                subtask = entity.toTaskDto()
                            )
                            if (result.isSuccessful) {
                                dao.insertSubtask(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                            } else handleErrorCode(result.code())
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
                            val result = api.deleteTaskList(entity.id)
                            if (result.isSuccessful) {
                                dao.deleteTasklist(entity)
                                true
                            } else {
                                handleErrorCode(result.code())
                                false
                            }
                        }

                        PendingActions.UPDATE -> {
                            val result = api.updateTaskList(
                                taskListId = entity.id,
                                taskList = entity.toTaskListUpdateDto()
                            )
                            if (result.isSuccessful) {
                                dao.updateTasklist(
                                    entity.markAsSynchronized(
                                        parseIsoDate(result.body()?.updated)
                                    )
                                )
                                true
                            } else {
                                handleErrorCode(result.code())
                                false
                            }
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
                            } else {
                                handleErrorCode(result.code())
                                false
                            }
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
                        handleErrorCode(response.code())
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