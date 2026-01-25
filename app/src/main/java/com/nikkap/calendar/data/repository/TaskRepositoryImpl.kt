package com.nikkap.calendar.data.repository

import com.nikkap.calendar.core.auth.AuthManager
import com.nikkap.calendar.data.mapper.toTask
import com.nikkap.calendar.data.remote.api.TasksApi
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.repository.TaskRepository

class TaskRepositoryImpl(
    private val api: TasksApi,
    private val authManager: AuthManager
) : TaskRepository {
    override suspend fun getTasks(): List<Task> {
        val token = authManager.getAccessToken()
            ?: throw Exception("Не удалось получить токен")
//        TODO(isSuccessful)
        val response = api.getUserTasks(token = "Bearer $token")
        return response.body()?.items?.map { it.toTask() } ?: emptyList()
    }
}