package com.nikkap.calendar.domain.repository

import com.nikkap.calendar.domain.model.Task

interface TaskRepository {
    suspend fun getTasks(): List<Task>
}