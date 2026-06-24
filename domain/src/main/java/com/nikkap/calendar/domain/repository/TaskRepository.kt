package com.nikkap.calendar.domain.repository

import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun syncAllTasks(): Result<Unit>
    suspend fun getTask(id: String): Task
    suspend fun getSubtasksByParentId(parentId: String): List<Subtask>
    fun getNonDeleteTasks(): Flow<List<Task>>
    fun getNonDeleteTaskLists(): Flow<List<TaskList>>
    fun getNonDeleteSubtasks(): Flow<List<Subtask>>
    suspend fun saveTask(task: Task)
    suspend fun saveSubtask(subtask: Subtask)
    fun saveSubtasks(list: List<Subtask>)
    suspend fun saveTasklist(taskList: TaskList)
    suspend fun updateTask(task: Task)
    suspend fun updateSubtask(subtask: Subtask)
    suspend fun updateSubtasks(list: List<Subtask>)
    suspend fun updateTaskList(taskList: TaskList)
    suspend fun deleteTask(id: String)
    suspend fun deleteSubtask(id: String)
    suspend fun completeSubtask(id: String)
    suspend fun completeTask(id: String)
}