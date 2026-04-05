package com.nikkap.calendar.domain.repository

import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun syncAllTasks(): Result<Unit>
    val allTasks: Flow<List<Task>>
    val allSubtasks: Flow<List<Subtask>>
    val allTaskLists: Flow<List<TaskList>>
    suspend fun getTask(id: String): Task
    suspend fun saveTask(task: Task)
    suspend fun updateTask(task: Task)
}