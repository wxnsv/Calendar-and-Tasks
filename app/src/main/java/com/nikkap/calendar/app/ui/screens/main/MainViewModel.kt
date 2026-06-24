package com.nikkap.calendar.app.ui.screens.main

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
import androidx.work.workDataOf
import com.nikkap.calendar.app.core.auth.AuthentificationManager
import com.nikkap.calendar.app.ui.navigation.NavEvent
import com.nikkap.calendar.app.ui.navigation.NavigationTarget
import com.nikkap.calendar.data.local.prefs.UserPrefs
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import com.nikkap.calendar.data.worker.SavePhotoWorker
import com.nikkap.calendar.data.worker.SyncWorker
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
import okhttp3.internal.platform.PlatformRegistry.applicationContext
import java.io.File
import java.util.concurrent.TimeUnit

class MainViewModel(
    private val tasksRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val userPrefRepository: UserPreferencesRepository,
    private val workManager: WorkManager,
    private val authentificationManager: AuthentificationManager,
) : ViewModel() {

    private val _navigationEvent = Channel<NavEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _snackbarEvent = MutableSharedFlow<SnackbarMessage>()
    val snackbarEvent: SharedFlow<SnackbarMessage> = _snackbarEvent


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
            setIsMainReady()
            state.copy(
                userState = prefs,
                isScreensReady = isReady,
                isLoading = !isReady,
                isPrefsLoaded = true
            )
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

    fun showSnackbar(message: String, actionText: String, onUndo: () -> Unit) {
        viewModelScope.launch {
            _snackbarEvent.emit(SnackbarMessage(message, actionText, onUndo))
        }
    }

    fun setListReady() {
        _isListReady.value = true
    }

    fun setSplitReady() {
        _isSplitReady.value = true
    }

    fun setIsMainReady() {
        _isMainReady.value = true
    }

    fun setIsAuthReady() {
        _isAuthReady.value = true
    }

    private fun setAuthorizeScreensReady() {
        _isSplitReady.value = true
        _isListReady.value = true
    }


    fun checkAuthAndNavigate() {
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
            } else if (isAuthorized) syncData()
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
            val constraint =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                1, TimeUnit.HOURS,
                5, TimeUnit.MINUTES
            )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setConstraints(constraint)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "AuthorizePeriodicSync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )

            while (isActive) {
                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .build()

                workManager.enqueueUniqueWork(
                    "RegularSyncWorker",
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

    fun authorizeSuccess(url: String) {
        viewModelScope.launch {
            userPrefRepository.completeFirstLaunch()

            val constraint =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                1, TimeUnit.HOURS,
                5, TimeUnit.MINUTES
            )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setConstraints(constraint)
                .build()

            val savePhotoRequest = OneTimeWorkRequestBuilder<SavePhotoWorker>()
                .setInputData(workDataOf("PHOTO_URL" to url))
                .build()
            workManager.enqueue(savePhotoRequest)
            workManager.enqueueUniquePeriodicWork(
                "AuthorizePeriodicSync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )
            startActiveSync()

            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Pager
                )
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

    fun logout() {
        viewModelScope.launch {
            authentificationManager.signOutUser()
            userPrefRepository.clearSession()
            tasksRepository.clearAll()
            calendarRepository.clearAll()

            val fileName = "user_avatar.jpg"
            val file = File(applicationContext!!.filesDir, fileName)

            if (file.exists()) {
                file.delete()
            }

            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Auth
                )
            )
        }
    }

    private fun syncData() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .build()

        workManager.enqueueUniqueWork(
            "OpenAppSync",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }
}