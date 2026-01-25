package com.nikkap.calendar.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.domain.model.CalendarItem
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val tasksRepository: TaskRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val _itemTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _itemEvents = MutableStateFlow<List<CalendarItem>>(emptyList())
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = combine(
        _state,
        _itemTasks,
        _itemEvents
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

    fun loadItems() {
        viewModelScope.launch {
            val tasksResult = tasksRepository.getTasks()
            val eventsResult = calendarRepository.getCalendarItems()

            _itemTasks.value = tasksResult
            _itemEvents.value = eventsResult

            Log.d("Response", "Tasks loaded: ${tasksResult.size}")
            tasksResult.forEach { Log.d("Response", "$it") }
            Log.d("Response", "Events loaded: ${eventsResult.size}")
            eventsResult.forEach { Log.d("Response", "$it") }
        }
    }
}
