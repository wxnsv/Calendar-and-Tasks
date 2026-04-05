package com.nikkap.calendar.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SyncWorker(
    appContext: Context, params: WorkerParameters,
    private val calendarRepository: CalendarRepository,
    private val taskRepository: TaskRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return coroutineScope {
            val calendarResult = async { calendarRepository.syncCalendar() }
            val taskResult = async { taskRepository.syncAllTasks() }
            if (calendarResult.await().isSuccess && taskResult.await().isSuccess) Result.success()
            else Result.retry()
        }
    }
}