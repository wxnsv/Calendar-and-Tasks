package com.nikkap.calendar.app.ui.screens.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.nikkap.calendar.app.ui.navigation.NavEvent
import com.nikkap.calendar.app.ui.navigation.NavigationTarget
import com.nikkap.calendar.data.local.prefs.UserPrefs
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import com.nikkap.calendar.data.worker.SyncWorker
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
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
    private val userPrefRepository: UserPreferencesRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _navigationEvent = Channel<NavEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()


    private val _prefsFlow = userPrefRepository.userStateFlow.map<UserPrefs, UserPrefs?> { it }
        .onStart { emit(null) }
    private val _isListReady = MutableStateFlow(false)
    private val _isSplitReady = MutableStateFlow(false)
    private val _isMainReady = MutableStateFlow(false)
    private val _isAuthReady = MutableStateFlow(false)
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = combine(
        _state,
        _prefsFlow,
        _isListReady,
        _isSplitReady,
        _isMainReady,
        _isAuthReady
    ) { arrayOfFlows ->
        val state = arrayOfFlows[0] as MainState
        val prefs = arrayOfFlows[1] as? UserPrefs
        val isListReady = arrayOfFlows[2] as Boolean
        val isSplitReady = arrayOfFlows[3] as Boolean
        val isMainReady = arrayOfFlows[4] as Boolean
        val isAuthReady = arrayOfFlows[5] as Boolean


        if (prefs != null && state.isLoading) {
            val isReady =
                isMainReady && isListReady && isSplitReady && !prefs.isFirstLaunch || isAuthReady
            if (prefs.isFirstLaunch) setAuthorizeScreensReady()
            setIsMainReadyTrue()
            state.copy(userState = prefs, isScreensReady = isReady, isLoading = !isReady)
        } else state.copy(isScreensReady = false)
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

    fun setListReady() {
        _isListReady.value = true
    }

    fun setSplitReady() {
        _isSplitReady.value = true
    }

    fun setIsMainReadyTrue() {
        _isMainReady.value = true
    }

    fun setIsAuthReadyTrue() {
        _isAuthReady.value = true
    }

    private fun setAuthorizeScreensReady() {
        _isSplitReady.value = true
        _isListReady.value = true
    }


    fun checkAuthAndNavigate(context: Context) {
        viewModelScope.launch {
            _isMainReady.first { it }
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
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .build()

                workManager.enqueueUniqueWork(
                    "OneTimeSyncWorker",
                    ExistingWorkPolicy.KEEP,
                    syncRequest
                )
                delay(3 * 60 * 1000L)
            }
        }
    }

    fun toAboutScreen() {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(NavigationTarget.About)
            )
        }
    }

    fun toSettingsScreen() {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(NavigationTarget.Settings)
            )
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
            ) // TODO
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                "AuthorizePeriodicSync",
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

    private fun syncData(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "OneTimeSyncWorker",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
        // TODO (exceptions)
    }
}