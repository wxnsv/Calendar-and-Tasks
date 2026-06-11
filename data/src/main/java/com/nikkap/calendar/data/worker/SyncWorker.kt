package com.nikkap.calendar.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nikkap.calendar.core.exceptions.NetworkException
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

class SyncWorker(
    appContext: Context, params: WorkerParameters,
    private val calendarRepository: CalendarRepository,
    private val taskRepository: TaskRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = supervisorScope {
        val calendarDeferred = async { calendarRepository.syncCalendar() }
        val tasksDeferred = async { taskRepository.syncAllTasks() }

        val results = listOf(
            calendarDeferred.await(),
            tasksDeferred.await()
        )

        if (results.all { it.isSuccess }) {
            Result.success()
        } else {
            val firstException = results.firstNotNullOfOrNull { it.exceptionOrNull() }

            val errorPayload = when (firstException) {
                is NetworkException.NoInternetException -> workDataOf(
                    "SYNC_ERROR_MESSAGE" to "No internet connection",
                )

                is NetworkException.UnauthorizedException -> workDataOf(
                    "SYNC_ERROR_MESSAGE" to "${firstException.message}",
                )

                is NetworkException.ServerException -> workDataOf(
                    "SYNC_ERROR_MESSAGE" to "${firstException.message}"
                )

                else -> workDataOf(
                    "SYNC_ERROR_MESSAGE" to "UNKNOWN_ERROR",
                )
            }
            Result.failure(errorPayload)
        }
    }
}