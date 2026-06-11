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
import com.nikkap.calendar.data.remote.dto.update.TaskUpdateDto
import com.nikkap.calendar.data.utils.localSyncEntities
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
        val result = api.createTask(
            taskDto = task.toTaskDto(),
            taskListId = task.taskListId.ifBlank {
                userPrefRepository.defaultTasklistId.first() ?: ""
            }
        )
        if (result.isSuccessful) dao.updateTask(
            task.toTaskEntity()
                .markAsSynchronized(parseIsoDate(result.body()?.updated))
        )
    }

    override suspend fun saveSubtask(subtask: Subtask) {
        dao.insertSubtask(
            subtask.toSubtaskEntity()
                .changePendingAction(PendingActions.INSERT)
        )
        val result = api.createSubtask(
            taskListId = subtask.taskListId,
            subtask = subtask.toTaskUpdateDto()
        )
        if (result.isSuccessful) dao.updateSubtask(
            subtask.toSubtaskEntity()
                .markAsSynchronized(parseIsoDate(result.body()?.updated))
        )
    }

    override suspend fun saveTasklist(taskList: TaskList) {
        dao.insertTaskList(
            taskList.toTaskListEntity()
                .changePendingAction(PendingActions.INSERT)
        )
        val result = api.createTaskList(
            taskList = taskList.toTaskListDto()
        )
        if (result.isSuccessful) dao.updateTasklist(
            taskList.toTaskListEntity()
                .markAsSynchronized(parseIsoDate(result.body()?.updated))
        )
    }

    override suspend fun updateTask(task: Task) {
        val entity = dao.getTask(task.id!!)
        dao.updateTask(task.toTaskEntity().changePendingAction(PendingActions.UPDATE))
        val result = api.updateTask(
            task = task.toTaskUpdateDto(),
            taskListId = entity.taskListId.ifBlank { "@default" },
            taskId = task.id!!
        )
        if (result.isSuccessful) dao.updateTask(
            result.body()!!.toTaskEntity(task.taskListId)
                .markAsSynchronized(parseIsoDate(result.body()!!.updated))
        )
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
        )
    }

    override suspend fun updateTaskList(taskList: TaskList) {
        dao.updateTasklist(taskList.toTaskListEntity().changePendingAction(PendingActions.UPDATE))
        val result = api.updateTaskList(
            taskListId = taskList.id,
            taskList = taskList.toTaskListUpdateDto(),
        )
        if (result.isSuccessful) dao.updateTasklist(
            taskList.toTaskListEntity().markAsSynchronized(parseIsoDate(result.body()!!.updated))
        )
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
                        if (result.isSuccessful) countOfSuccess++
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
        }
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
        }
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
        }
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

        // tasklists
        localSyncEntities(
            taskLists,
            getLocalEntities = { dao.getNonDeleteTaskLists().first() },
            deleteEntitiesByIds = { dao.deleteTaskListsByIds(it) },
            insertEntities = { dao.insertTaskLists(it) }
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
                if (!it.deleted) it.toSubtaskEntity(taskListId) else it.toSubtaskEntity(
                    taskListId
                ).changePendingAction(PendingActions.DELETE)
            }
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

    private suspend fun tasksPendingSync(): Result<Unit> = coroutineScope {
        val pendingEntities = dao.getPendingTasks().first()

        val deferredResults: List<Deferred<Result<Unit>>> = pendingEntities.map { entity ->
            async {
                runCatching {
                    when (entity.pendingAction) {
                        PendingActions.DELETE -> {
                            val response = api.deleteTask(entity.taskListId, entity.id)
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Delete failed"
                            )
                            dao.deleteTask(entity.id)
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
                            val response = api.deleteSubtask(entity.taskListId, entity.id)
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Delete failed"
                            )
                            dao.deleteSubtask(entity.id)
                        }

                        PendingActions.UPDATE -> {
                            val response = api.updateSubtask(
                                taskListId = entity.taskListId, entity.id,
                                subtask = entity.toTaskUpdateDto()
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
                                subtask = entity.toTaskUpdateDto()
                            )
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Insert failed"
                            )

                            dao.insertSubtask(
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
                            dao.deleteTasklist(entity)
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
                            )

                            dao.insertTaskList(
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
            Result.failure(firstException ?: Exception("Some tasklists failed to sync with server"))
        }
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