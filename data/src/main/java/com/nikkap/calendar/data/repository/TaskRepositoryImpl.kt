package com.nikkap.calendar.data.repository

import com.nikkap.calendar.core.exceptions.NetworkException
import com.nikkap.calendar.core.utils.parseIsoDate
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
import com.nikkap.calendar.data.utils.localSyncEntities
import com.nikkap.calendar.data.utils.localSyncTaskLists
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.model.TaskList
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Collections.emptyList

class TaskRepositoryImpl(
    private val api: TasksApi,
    private val dao: TaskDao,
    private val userPrefRepository: UserPreferencesRepository
) : TaskRepository {

    override suspend fun getTask(id: String): Task {
        return dao.getTask(id).toTask()
    }

    override suspend fun getSubtasksByParentId(parentId: String): List<Subtask> {
        return dao.getSubtasksByParentId(parentId).map {
            it.toSubtask()
        }
    }

    override suspend fun getNonDeleteTasks(): List<Task> {
        return dao.getNonDeleteTasks().first().map {
            it.toTask()
        }
    }

    override suspend fun getNonDeleteTaskLists(): List<TaskList> {
        return dao.getNonDeleteTaskLists().first().map {
            it.toTaskList()
        }
    }

    override suspend fun getNonDeleteSubtasks(): List<Subtask> {
        return dao.getNonDeleteSubtasks().first().map {
            it.toSubtask()
        }
    }

    override suspend fun saveTask(task: Task) {
        dao.insertTask(
            task.toTaskEntity()
                .changePendingAction(PendingActions.INSERT)
        )
    }

    override suspend fun saveSubtask(subtask: Subtask) {
        dao.insertSubtask(
            subtask.toSubtaskEntity()
                .changePendingAction(PendingActions.INSERT)
        )
    }

    override suspend fun saveSubtasks(list: List<Subtask>) {
        list.forEach { subtask ->
            dao.insertSubtask(
                subtask.toSubtaskEntity()
                    .changePendingAction(PendingActions.INSERT)
            )
        }
    }

    override suspend fun saveTasklist(taskList: TaskList) {
        dao.insertTaskList(
            taskList.toTaskListEntity()
                .changePendingAction(PendingActions.INSERT)
        )
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task.toTaskEntity().changePendingAction(PendingActions.UPDATE))
    }

    override suspend fun updateSubtask(subtask: Subtask) {
        dao.updateSubtask(subtask.toSubtaskEntity().changePendingAction(PendingActions.UPDATE))
    }

    override suspend fun updateSubtasks(list: List<Subtask>) {
        list.forEach { subtask ->
            if (dao.isSubtaskExists(subtask.id)) {
                dao.updateSubtask(
                    subtask.toSubtaskEntity()
                        .changePendingAction(PendingActions.UPDATE)
                )
            } else dao.insertSubtask(
                subtask.toSubtaskEntity()
                    .changePendingAction(PendingActions.INSERT)
            )
        }
    }

    override suspend fun updateTaskList(taskList: TaskList) {
        dao.updateTasklist(taskList.toTaskListEntity().changePendingAction(PendingActions.UPDATE))
    }

    override suspend fun deleteTask(id: String) {
        dao.markAsDeleteTask(id, System.currentTimeMillis())
        dao.markAsDeleteSubtasksOfTask(id, System.currentTimeMillis())
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
                        val result = api.deleteSubtask(taskListId, parentTaskId, subtaskId)
                        if (result.isSuccessful) countOfSuccess++
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

    override suspend fun deleteSubtask(id: String) {
        dao.markAsDeleteSubtask(id, System.currentTimeMillis())
    }

    override suspend fun completeSubtask(id: String) {
        dao.completeSubtask(id, System.currentTimeMillis())
    }

    override suspend fun completeTask(id: String) {
        dao.completeTask(
            id, System.currentTimeMillis()
        )
    }


    private suspend fun getRemoteTaskLists(): Result<List<TaskListDto>?> {
        val taskLists = api.getTaskLists()
        if (taskLists.isSuccessful) {
            val entities = taskLists.body()?.items
            return Result.success(entities)
        } else {
            return Result.failure(Exception("Failed to sync tasks with ${taskLists.code()} code"))
        }
    }

    override suspend fun syncAllTasks(): Result<Unit> = try {
        val remoteTaskLists = getRemoteTaskLists()

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

        localSyncTaskLists(
            taskLists,
            getLocalLists = { dao.getNonDeleteTaskLists().first() },
            deleteListsByIds = { dao.deleteTaskListsByIds(it) },
            insertLists = { dao.insertTaskLists(it) },
            deleteListAndTasks = { deleteTaskListAndTasks(it) }
        )

        val listsPendingResult = tasklistsPendingSync()

        if (listsPendingResult.isFailure) {
            return listsPendingResult
        }

        if (userPrefRepository.defaultTasklistId.first() == null) {
            val result = api.getTasklist()
            if (result.isSuccessful) userPrefRepository.setDefaultTasklistId(result.body()!!.id!!)
        }

        val tasksAndTakListIds =
            getTasksWithTaskListIds(remoteTaskLists = taskLists)

        if (tasksAndTakListIds.isEmpty()) {
            return Result.success(Unit)
        }

        val taskEntitiesToSync = tasksAndTakListIds.flatMap { (taskListId, tasks) ->
            val (rootTasks, _) = tasks.partition { it.parent == null }
            rootTasks.map {
                if (!it.deleted) it.toTaskEntity(taskListId) else it.toTaskEntity(
                    taskListId
                ).changePendingAction(PendingActions.DELETE)
            }
        }

        val subtaskEntitiesToSync = tasksAndTakListIds.flatMap { (taskListId, tasks) ->
            val (_, subtasks) = tasks.partition { it.parent == null }
            subtasks.map {
                if (!it.deleted) it.toSubtaskEntity(taskListId) else {
                    it.toSubtaskEntity(
                        taskListId
                    ).changePendingAction(PendingActions.DELETE)
                }
            }
        }

        /*
        * this code needed bcs if task was deleted,
        * its subtasks be returned with 'parent' = null
        */
        tasksAndTakListIds.forEach { (_, tasks) ->
            val allTaskToDelete = tasks.filter { it.deleted }.map { it.id }.toSet()
            dao.deleteSubtasksByIds(allTaskToDelete.toList())
        }


        coroutineScope {
            val tasksResultDeferred = async {
                localSyncEntities(
                    taskEntitiesToSync,
                    { dao.getNonDeleteTasks().first() },
                    { dao.deleteTasksByIds(it) },
                    { dao.insertTasks(it) }
                )
            }

            val subtasksResultDeferred = async {
                localSyncEntities(
                    subtaskEntitiesToSync,
                    { dao.getNonDeleteSubtasks().first() },
                    { dao.deleteSubtasksByIds(it) },
                    { dao.insertSubtasks(it) }
                )
            }
            tasksResultDeferred.await()
            subtasksResultDeferred.await()

            tasksPendingSync()
            subtasksPendingSync()
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun deleteTaskListAndTasks(deleteTaskListId: String) {
        dao.deleteTasklistById(deleteTaskListId)
        dao.deleteTasksByTasklistId(deleteTaskListId)
        dao.deleteSubtasksByTasklistId(deleteTaskListId)
    }

    private suspend fun tasksPendingSync(): Result<Unit> = coroutineScope {
        val pendingEntities = dao.getPendingTasks().first()

        val deferredResults: List<Deferred<Result<Unit>>> = pendingEntities.map { entity ->
            async {
                runCatching {
                    when (entity.pendingAction) {
                        PendingActions.DELETE -> {
                            val response = api.deleteTask(entity.taskListId, entity.id)
                            if (response.isSuccessful) deleteAllSubtasks(
                                entity.taskListId,
                                entity.id
                            )
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Delete failed"
                            )
                        }

                        PendingActions.UPDATE -> {
                            val response = api.updateTask(
                                taskListId = entity.taskListId, entity.id,
                                task = entity.toTaskUpdateDto()
                            )
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Update failed"
                            )

                            dao.updateTask(
                                entity.markAsSynchronized(parseIsoDate(response.body()?.updated))
                            )
                        }

                        PendingActions.INSERT -> {
                            val response = api.createTask(
                                taskListId = entity.taskListId,
                                taskDto = entity.toTaskDto()
                            )
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Insert failed"
                            )

                            dao.insertTask(
                                entity.markAsSynchronized(parseIsoDate(response.body()?.updated))
                            )
                        }

                        PendingActions.NONE -> {}
                    }
                }
            }
        }
        val results: List<Result<Unit>> = deferredResults.awaitAll()

        if (results.all { it.isSuccess }) {
            Result.success(Unit)
        } else {
            val firstException = results.firstNotNullOfOrNull { it.exceptionOrNull() }
            Result.failure(firstException ?: Exception("Some tasks failed to sync with server"))
        }
    }

    private suspend fun subtasksPendingSync(): Result<Unit> = coroutineScope {
        val pendingEntities = dao.getPendingSubtasks().first()

        val deferredResults: List<Deferred<Result<Unit>>> = pendingEntities.map { entity ->
            async {
                runCatching {
                    when (entity.pendingAction) {
                        PendingActions.DELETE -> {
                            val response =
                                api.deleteSubtask(entity.taskListId, entity.parentId, entity.id)
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Delete failed"
                            )
                            dao.deleteSubtask(entity.id)
                        }

                        PendingActions.UPDATE -> {
                            val response = api.updateSubtask(
                                taskListId = entity.taskListId, entity.id,
                                subtask = entity.toTaskUpdateDto(),
                            )
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Update failed"
                            )

                            dao.updateSubtask(
                                entity.markAsSynchronized(parseIsoDate(response.body()?.updated))
                            )
                        }

                        PendingActions.INSERT -> {
                            val response = api.createSubtask(
                                taskListId = entity.taskListId,
                                subtask = entity.toTaskUpdateDto(),
                                parentTaskId = entity.parentId
                            )
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Insert failed"
                            ) else {
                                dao.updateSubtask(
                                    response.body()!!.toSubtaskEntity(entity.taskListId)
                                )
                                dao.deleteSubtask(entity.id)
                            }
                        }

                        PendingActions.NONE -> {}
                    }
                }
            }
        }
        val results: List<Result<Unit>> = deferredResults.awaitAll()

        if (results.all { it.isSuccess }) {
            Result.success(Unit)
        } else {
            val firstException = results.firstNotNullOfOrNull { it.exceptionOrNull() }
            Result.failure(firstException ?: Exception("Some subtasks failed to sync with server"))
        }
    }

    private suspend fun tasklistsPendingSync(): Result<Unit> = coroutineScope {
        val pendingEntities = dao.getPendingTaskLists().first()

        val deferredResults: List<Deferred<Result<Unit>>> = pendingEntities.map { entity ->
            async {
                runCatching {
                    when (entity.pendingAction) {
                        PendingActions.DELETE -> {
                            val response = api.deleteTaskList(entity.id)
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Delete failed"
                            )
                            deleteTaskListAndTasks(entity.id)
                        }

                        PendingActions.UPDATE -> {
                            val response =
                                api.updateTaskList(entity.id, entity.toTaskListUpdateDto())
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Update failed"
                            )

                            dao.updateTasklist(
                                entity.markAsSynchronized(parseIsoDate(response.body()?.updated))
                            )
                        }

                        PendingActions.INSERT -> {
                            val response = api.createTaskList(entity.toTaskListDto())
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Insert failed"
                            ) else {
                                changeIdOfTasklistAndTasks(
                                    entity.id,
                                    response.body()?.id!!,
                                    response.body()?.toTaskListEntity()!!
                                )
                            }
                        }

                        PendingActions.NONE -> {}
                    }
                }
            }
        }

        val results: List<Result<Unit>> = deferredResults.awaitAll()

        if (results.all { it.isSuccess }) {
            Result.success(Unit)
        } else {
            val firstException = results.firstNotNullOfOrNull { it.exceptionOrNull() }
            Result.failure(firstException ?: Exception("Some tasklists failed to sync with server"))
        }
    }

    private suspend fun changeIdOfTasklistAndTasks(
        oldId: String,
        newId: String,
        newTasklist: TaskListEntity
    ) {
        dao.deleteTasklistById(oldId)
        dao.insertTaskList(newTasklist)

        dao.updateSubtasksTasklistId(oldId, newId)
        dao.updateTasksTasklistId(oldId, newId)
    }

    private suspend fun getTasksWithTaskListIds(
        remoteTaskLists: List<TaskListEntity>,
    ): List<Pair<String, List<TaskDto>>> = coroutineScope {
        val taskLists = remoteTaskLists.map { it.toTaskList() }

        val deferredResults = taskLists.map { taskList ->
            async<Pair<String, List<TaskDto>>> {
                val response = api.getTasks(
                    taskListId = taskList.id,
                )

                if (response.isSuccessful) {
                    taskList.id to (response.body()?.items ?: emptyList())
                } else {
                    throw Exception("API Error ${response.code()}: Failed to fetch tasks for list ${taskList.id}")
                }
            }
        }
        return@coroutineScope deferredResults.awaitAll()
    }

}