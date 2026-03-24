package com.nikkap.calendar.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository

class SyncWorker(
    appContext: Context, params: WorkerParameters,
    private val calendarRepository: CalendarRepository, private val taskRepository: TaskRepository
) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        calendarRepository.syncCalendar()
        taskRepository.syncTasks()

        return Result.success()
    }
}