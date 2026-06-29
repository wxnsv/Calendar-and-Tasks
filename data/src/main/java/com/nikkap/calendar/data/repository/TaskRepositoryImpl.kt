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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
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
    private val userPrefRepository: UserPreferencesRepository,
    private val appScope: CoroutineScope
) : TaskRepository {

    override suspend fun getTask(id: String): Task {
        return dao.getTask(id).toTask()
    }

    override suspend fun getSubtasksByParentId(parentId: String): List<Subtask> {
        return dao.getSubtasksByParentId(parentId).map {
            it.toSubtask()
        }
    }

    override fun getNonDeleteTasks(): Flow<List<Task>> {
        return dao.getNonDeleteTasks().map {
            it.map { it.toTask() }
        }
    }

    override fun getNonDeleteTaskLists(): Flow<List<TaskList>> {
        return dao.getNonDeleteTaskLists().map {
            it.map { it.toTaskList() }
        }
    }

    override fun getNonDeleteSubtasks(): Flow<List<Subtask>> {
        return dao.getNonDeleteSubtasks().map {
            it.map { it.toSubtask() }
        }
    }

    override suspend fun saveTask(task: Task) {
        appScope.launch {
            dao.insertTask(
                task.toTaskEntity()
                    .changePendingAction(PendingActions.INSERT)
            )
        }
    }

    override suspend fun saveSubtask(subtask: Subtask) {
        appScope.launch {
            dao.insertSubtask(
                subtask.toSubtaskEntity()
                    .changePendingAction(PendingActions.INSERT)
            )
        }
    }

    override fun saveSubtasks(list: List<Subtask>) {
        appScope.launch {
            val latestPositionSubtask = dao.getLastPositionSubtask(list.first().parentId)
            var currentPosition = latestPositionSubtask?.position?.toLong() ?: -1
            list.forEach { subtask ->
                currentPosition++
                val insertSubtask = subtask.copy(position = currentPosition.toString())
                dao.insertSubtask(
                    insertSubtask.toSubtaskEntity()
                        .changePendingAction(PendingActions.INSERT)
                )
            }
        }
    }

    override suspend fun saveTasklist(taskList: TaskList) {
        appScope.launch {
            dao.insertTaskList(
                taskList.toTaskListEntity()
                    .changePendingAction(PendingActions.INSERT)
            )
        }

    }

    override suspend fun updateTask(task: Task) {
        appScope.launch {
            dao.updateTask(task.toTaskEntity().changePendingAction(PendingActions.UPDATE))
        }
    }

    override suspend fun updateSubtask(subtask: Subtask) {
        appScope.launch {
            dao.updateSubtask(subtask.toSubtaskEntity().changePendingAction(PendingActions.UPDATE))
        }
    }

    override suspend fun updateSubtasks(list: List<Subtask>) {
        appScope.launch {
            list.forEach { subtask ->
                dao.updateSubtask(
                    subtask.toSubtaskEntity()
                        .changePendingAction(PendingActions.UPDATE)
                )
            }
        }
    }

    override suspend fun updateTaskList(taskList: TaskList) {
        appScope.launch {
            dao.updateTasklist(
                taskList.toTaskListEntity().changePendingAction(PendingActions.UPDATE)
            )
        }
    }

    override suspend fun deleteTask(id: String) {
        appScope.launch {
            dao.markAsDeleteTask(id, System.currentTimeMillis())
            dao.markAsDeleteSubtasksOfTask(id, System.currentTimeMillis())
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
        appScope.launch {
            dao.markAsDeleteSubtask(id, System.currentTimeMillis())
        }
    }

    override suspend fun completeSubtask(id: String) {
        appScope.launch {
            dao.completeSubtask(id, System.currentTimeMillis())
        }
    }

    override suspend fun completeTask(id: String) {
        appScope.launch {
            dao.completeTask(
                id, System.currentTimeMillis()
            )
        }
    }

    override fun clearAll() {
        appScope.launch {
            dao.clearSubtasks()
            dao.clearTasks()
            dao.clearTaskLists()
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

        tasksPendingSync()
        subtasksPendingSync()

        coroutineScope {
            localSyncEntities(
                subtaskEntitiesToSync,
                { dao.getAllSubtasks() },
                { dao.deleteSubtasksByIds(it) },
                { dao.insertSubtasks(it) }
            )


                localSyncEntities(
                    taskEntitiesToSync,
                    { dao.getNonDeleteTasks().first() },
                    { dao.deleteTasksByIds(it) },
                    { dao.insertTasks(it) }
                )
            }

            Result.success(Unit)
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
                            ) else {
                                val task = response.body()!!
                                dao.deleteTask(entity.id)
                                dao.insertTask(task.toTaskEntity(entity.taskListId))
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
                            ) else dao.deleteSubtask(entity.id)
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
                            val previousPosition = entity.position.toInt() - 1
                            val previousSubtaskId = dao.getSubtaskIdByPosition(
                                entity.parentId,
                                previousPosition.toString()
                            )
                            val response = api.createSubtask(
                                taskListId = entity.taskListId,
                                subtask = entity.toTaskUpdateDto(),
                                parentTaskId = entity.parentId,
                                previousSubtaskId = previousSubtaskId
                            )
                            if (!response.isSuccessful) throw NetworkException.ServerException(
                                response.code(),
                                "Insert failed"
                            ) else {
                                val subtaskDto = response.body()!!
                                dao.deleteSubtask(entity.id)
                                dao.insertSubtask(subtaskDto.toSubtaskEntity(entity.taskListId))
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