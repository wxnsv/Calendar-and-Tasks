package com.nikkap.calendar.ui.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import com.nikkap.calendar.ui.screens.auth.AuthState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val tasksRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val userPrefRepository: UserPreferencesRepository
) : ViewModel() {
    private val _navigationEvent = Channel<NavEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state = _state.asStateFlow()
    fun checkAuthAndNavigate() {
        viewModelScope.launch {
            val isLoggedIn = hasData()
            if (isLoggedIn) {
                _navigationEvent.send(NavEvent.ToList)
            } else {
                _navigationEvent.send(NavEvent.ToAuth)
            }
        }
    }

    fun onTaskClicked() {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.ToCreate(
                    "TASK", ""
                )
            )
        }
    }

    fun onListItemClicked(id: String, type: String) {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.ToCreate(
                    itemId = id,
                    type = type
                )
            )
        }
    }

    fun onEventClicked() {
        viewModelScope.launch {
            _navigationEvent.send(NavEvent.ToCreate("EVENT", ""))
        }
    }

    private fun start() {
        viewModelScope.launch {
            _navigationEvent.send(NavEvent.Start)
        }
    }

    fun onBirthdayClicked() {
        viewModelScope.launch {
            _navigationEvent.send(NavEvent.ToCreate("BIRTHDAY", ""))
        }
    }

    private suspend fun hasData(): Boolean {
        var hasData = false
// TODO("Refactor")
        val checkTasksData = tasksRepository.haveLocalData()
        val checkCalendarData = calendarRepository.haveLocalData()
        Log.d("AppAuth", "Have local tasks: $checkTasksData")
        Log.d("AppAuth", "Have local calendar: $checkCalendarData")
        if (checkTasksData && checkCalendarData) {
            hasData = true
        } else {
            val taskResult = tasksRepository.syncTasks()
            val calendarResult = calendarRepository.syncCalendar()
            if (calendarResult.isSuccess && taskResult.isSuccess) {
                hasData = true
            }
        }
        Log.d("AppAuth", "Has data: $hasData")
        return hasData
    }
}