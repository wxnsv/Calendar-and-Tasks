package com.nikkap.calendar.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.data.repository.UserPreferencesRepository
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


    fun checkAuthAndNavigate() {
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
                syncData()
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

    fun authorizeSuccess() {
        viewModelScope.launch {
            userPrefRepository.completeFirstLaunch()
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.List
                )
            )
        }
    }

    fun onTaskClicked() {
        viewModelScope.launch {
            _navigationEvent.send(
                NavEvent.NavigateTo(
                    NavigationTarget.Create(
                        "TASK", ""
                    ), NavigationTarget.List
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
                    ), NavigationTarget.List
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

    private suspend fun syncData() {
        tasksRepository.syncTasks()
        calendarRepository.syncCalendar()
        // TODO (exceptions)
    }
}