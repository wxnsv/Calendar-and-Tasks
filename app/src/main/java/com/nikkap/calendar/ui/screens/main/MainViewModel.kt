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
import com.nikkap.calendar.data.local.prefs.UserPrefs
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import com.nikkap.calendar.data.worker.SyncWorker
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import com.nikkap.calendar.ui.navigation.NavEvent
import com.nikkap.calendar.ui.navigation.NavigationTarget
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(
    private val tasksRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val userPrefRepository: UserPreferencesRepository
) : ViewModel() {

    private val _navigationEvent = Channel<NavEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()
    private val _prefsFlow = userPrefRepository.userStateFlow.map<UserPrefs, UserPrefs?> { it }
        .onStart { emit(null) }
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = combine(
        _state,
        _prefsFlow,
    ) { state, prefs ->

        if (prefs == null) {
            state.copy(isLoading = true)
        } else {
            state.copy(
                userState = prefs,
                isLoading = false,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainState()
    )

    init {
        viewModelScope.launch {
            state.first { !it.isLoading }
            val prefs = state.value.userState
            if (prefs.isAuthorized) startActiveSync()
        }
    }

    fun checkAuthAndNavigate(context: Context) {
        viewModelScope.launch {
            state.first { !it.isLoading }
            val prefs = state.value.userState
            val isFirst = prefs.isFirstLaunch
            val isAuthorized = prefs.isAuthorized
            if (isFirst) {
                _navigationEvent.send(
                    NavEvent.SetRoot(
                        NavigationTarget.Auth
                    )
                )
            } else if (isAuthorized) syncData(context)
            _navigationEvent.send(
                NavEvent.SetRoot(
                    NavigationTarget.Pager
                )
            )
        }

    }

    fun popBackStack() {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.PopBack
            )
        }
    }

    private fun startActiveSync() {
        viewModelScope.launch {
            while (isActive) {
                val tasksDeferred = async { tasksRepository.syncAllTasks() }
                val calendarDeferred = async { calendarRepository.syncCalendar() }

                tasksDeferred.await()
                calendarDeferred.await()
                delay(3 * 60 * 1000L)
            }
        }
    }

    fun authorizeSuccess(context: Context) {
        viewModelScope.launch {
            userPrefRepository.completeFirstLaunch()
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Pager
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

    fun onEditListItemClicked(id: String, type: String) {
        when (type) {
            "BIRTHDAY" -> toCreateBirthday(id)
            "EVENT" -> toCreateEvent(id)
            "TASK" -> toCreateTask(id)
        }
    }

    fun onDeleteListItemClicked(id: String, type: String) {
        viewModelScope.launch {
            when (type) {
                "BIRTHDAY" -> calendarRepository.deleteBirthday(id)
                "EVENT" -> calendarRepository.deleteEvent(id)
                "TASK" -> tasksRepository.deleteTask(id)
                "SUBTASK" -> tasksRepository.deleteSubtask(id)
            }
        }
    }

    fun onCompleteListItemClicked(id: String, type: String) {
        viewModelScope.launch {
            when (type) {
                "SUBTASK" -> tasksRepository.completeSubtask(id)
                "TASK" -> tasksRepository.completeTask(id)
            }
        }
    }

    fun toCreateTask(id: String = "") {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Create(
                        "TASK", id
                    )
                )
            )
        }
    }

    fun toCreateEvent(id: String = "") {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Create("EVENT", id)
                )
            )
        }
    }

    fun toCreateBirthday(id: String = "") {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Create("BIRTHDAY", id)
                )
            )
        }
    }

    fun toCreateSubtask(id: String = "") {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Create("BIRTHDAY", id)
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