package com.nikkap.calendar.ui.auth

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.core.auth.AuthManager
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authManager: AuthManager,
    private val tasksRepository: TaskRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state = _state.asStateFlow()

    fun checkAuth() {
        authManager.silentAuthorize(
            onSuccess = { _state.value = AuthState.Authenticated },
            onFailure = { _state.value = AuthState.Unauthenticated }
        )
    }
    fun checkData() {
        viewModelScope.launch {
            val checkTasksData = tasksRepository.haveLocalData()
            val checkCalendarData = calendarRepository.haveLocalData()
            Log.d("AppAuth", "Have local tasks: $checkTasksData")
            Log.d("AppAuth", "Have local tasks: $checkCalendarData")
            if (checkTasksData && checkCalendarData) {
                _state.value = AuthState.NavigateToMain
            } else {
                val tasksResult = tasksRepository.syncTasks()
                val calendarResult = calendarRepository.syncCalendar()
                if (tasksResult.isSuccess && calendarResult.isSuccess) {
                    _state.value = AuthState.NavigateToMain
                }
            }
        }
    }

    fun onAuthIntentReady(
        intentSender: IntentSender,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val request = IntentSenderRequest.Builder(intentSender).build()
        launcher.launch(request)
    }

    fun handleAuthResult(intent: Intent?) {
        authManager.handleActivityResult(intent) { token ->
            if (token != null) {
                _state.value = AuthState.Authenticated
            } else {
                _state.value = AuthState.Unauthenticated
            }
        }
    }
}
