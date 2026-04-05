package com.nikkap.calendar.ui.screens.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import com.nikkap.calendar.data.worker.SyncWorker
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import com.nikkap.calendar.ui.navigation.NavEvent
import com.nikkap.calendar.ui.navigation.NavigationTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class MainViewModel(
    private val tasksRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val userPrefRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _navigationEvent = Channel<NavEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    val prefs = runBlocking(Dispatchers.IO) {
        userPrefRepository.userStateFlow.first()
    }


    fun checkAuthAndNavigate(context: Context) {
        val isFirst = prefs.isFirstLaunch
        val isAuthorized = prefs.isAuthorized
        viewModelScope.launch {
            if (isFirst) {
                _navigationEvent.send(
                    NavEvent.SetRoot(
                        NavigationTarget.Auth
                    )
                )
            } else if (isAuthorized) {
                syncData(context)
                _navigationEvent.send(
                    NavEvent.SetRoot(
                        NavigationTarget.List
                    )
                )
            } else
                _navigationEvent.send(
                    NavEvent.SetRoot(
                        NavigationTarget.List
                    )
                )
        }

    }

    fun authorizeSuccess(context: Context) {
        viewModelScope.launch {
            userPrefRepository.completeFirstLaunch()
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.List
                )
            )


            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                30, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "PeriodicSync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }

    fun onTaskClicked() {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Create(
                        "TASK", ""
                    )
                )
            )
        }
    }

    fun onListItemClicked(id: String, type: String) {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Create(
                        itemId = id,
                        type = type
                    )
                )
            )
        }
    }

    fun onEventClicked() {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Create("EVENT", "")
                )
            )
        }
    }

    fun onBirthdayClicked() {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Create("BIRTHDAY", "")
                )
            )
        }
    }

    private fun syncData(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(
            syncRequest
        )
        // TODO (exceptions)
    }
}