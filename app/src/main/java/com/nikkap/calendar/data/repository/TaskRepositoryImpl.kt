package com.nikkap.calendar.data.repository

import com.nikkap.calendar.data.local.dao.TaskDao
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.mapper.toSubtask
import com.nikkap.calendar.data.mapper.toSubtaskEntity
import com.nikkap.calendar.data.mapper.toTask
import com.nikkap.calendar.data.mapper.toTaskDto
import com.nikkap.calendar.data.mapper.toTaskEntity
import com.nikkap.calendar.data.mapper.toTaskList
import com.nikkap.calendar.data.mapper.toTaskListEntity
import com.nikkap.calendar.data.remote.api.TasksApi
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.model.TaskList
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val api: TasksApi,
    private val dao: TaskDao,
) : TaskRepository {
    override val allTasks: Flow<List<Task>> = dao.getAllTasks()
        .map { entities ->
            entities.map { it.toTask() }
        }
    override val allSubtasks: Flow<List<Subtask>> = dao.getAllSubtasks()
        .map { entities ->
            entities.map { it.toSubtask() }
        }
    override val allTaskLists: Flow<List<TaskList>> = dao.getTaskLists()
        .map { entities ->
            entities.map { it.toTaskList() }
        }

    override suspend fun getTask(id: String): Task {
        return dao.getTask(id).toTask()
    }

    override suspend fun saveTask(task: Task) {
        api.createTask(taskDto = task.toTaskDto())
        dao.insertTask(task.toTaskEntity(PendingActions.INSERT))
    }

    override suspend fun updateTask(task: Task) {
        api.updateTask(
            task = task.toTaskDto(),
            taskId = task.id!!
        )
        dao.updateTask(task.toTaskEntity(PendingActions.UPDATE))
    }

    override suspend fun syncTasks(): Result<Unit> = try {
        val response = api.getTaskLists()
        if (response.isSuccessful) {
            val taskLists = response.body()?.items?.map { it.toTaskList() } ?: emptyList()
            val taskListEntities =
                response.body()?.items?.map { it.toTaskListEntity() } ?: emptyList()
            dao.insertTaskLists(taskListEntities)

            coroutineScope {
                taskLists.map { taskList ->
                    async { syncByTaskList(taskList) }
                }.awaitAll()
            }

            Result.success(Unit)
        } else {
            Result.failure(Exception("Error in syncTaskLists: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun haveLocalData(): Boolean {
        return dao.getCount() > 0
    }

    private suspend fun syncByTaskList(taskList: TaskList) {
        val response = api.getTasks(taskListId = taskList.id)
        if (response.isSuccessful) {
            val entities = response.body()?.items ?: emptyList()
            val (rootTasks, subTasks) = entities.partition { it.parent == null }
            val tasks = rootTasks.map { it.toTaskEntity(taskList.id) }
            val subtasks = subTasks.map { it.toSubtaskEntity() }
            dao.insertTasks(tasks)
            dao.insertSubtasks(subtasks)
        } else {
            throw Exception("Failed to sync list ${taskList.id}")
        }
    }
}