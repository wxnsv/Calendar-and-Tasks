package com.nikkap.calendar.data.repository

import com.nikkap.calendar.core.auth.AuthManager
import com.nikkap.calendar.data.local.dao.TaskDao
import com.nikkap.calendar.data.mapper.toTask
import com.nikkap.calendar.data.mapper.toTaskEntity
import com.nikkap.calendar.data.remote.api.TasksApi
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val api: TasksApi,
    private val authManager: AuthManager,
    private val dao: TaskDao
) : TaskRepository {
    override val allTasks: Flow<List<Task>> = dao.getAllTasks()
        .map { entities ->
            entities.map { it.toTask() }
        }

    override suspend fun haveLocalData(): Boolean {
        return dao.getCount() > 0
    }

    override suspend fun syncTasks(): Result<Unit> {
        return try {
            val token = authManager.getAccessToken()
            val response = api.getTasks(
                token = "Bearer $token",
            )

            if (response.isSuccessful) {
                val taskEntities = response.body()?.items?.map { it.toTaskEntity() } ?: emptyList()
                dao.insertTasks(taskEntities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}