package com.nikkap.calendar.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val tasksRepository: TaskRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val calendarItemsFlow = calendarRepository.allItems
    private val tasksFlow = tasksRepository.allTasks
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = combine(
        _state,
        tasksFlow,
        calendarItemsFlow
    ) { state, tasks, events ->
        val taskItems = tasks.map { ListItem.TaskItem(it) }
        val eventItems = events.map { ListItem.EventItem(it) }

        /**        Mixes tasks and events into a single list
        to provide in Recycler View**/
        val mixedList = (taskItems + eventItems).sortedBy { item ->
            when (item) {
                is ListItem.TaskItem -> item.task.title
                is ListItem.EventItem -> item.calendarItem.summary
            }
        }

        state.copy(
            items = mixedList,
            errorMessage = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainState()
    )

    fun refreshData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            withContext(Dispatchers.IO) { syncAll() }// вызов твоей функции синхронизации
            _state.update { it.copy(isLoading = false) }
        }
    }

    suspend fun syncAll() = coroutineScope {
        val calendarStatus = async { calendarRepository.syncCalendar() }
        val tasksStatus = async { tasksRepository.syncTasks() }

        calendarStatus.await()
        tasksStatus.await()

        Log.d("Response", "Tasks status: ${tasksStatus.isCompleted}")

        Log.d("Response", "Calendar status: ${calendarStatus.isCompleted}")
    }
}
