package com.nikkap.calendar.domain.repository

import com.nikkap.calendar.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun syncTasks(): Result<Unit>
    suspend fun haveLocalData(): Boolean
    val allTasks: Flow<List<Task>>
}