package com.nikkap.calendar.ui.screens.list

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

class ListViewModel(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val eventsFlow = calendarRepository.allEvents
    private val birthdayFlow = calendarRepository.allBirthdays
    private val tasksFlow = taskRepository.allTasks
    private val subtasksFlow = taskRepository.allSubtasks
    private val _state = MutableStateFlow(ListState())
    val state: StateFlow<ListState> = combine(
        _state,
        tasksFlow,
        eventsFlow,
        birthdayFlow,
        subtasksFlow

    ) { state, tasks, events, birthdays, subtasks ->
        val taskItems = tasks.filter { task -> !task.isCompleted }.map { ListItem.TaskItem(it) }
        val subtaskItems =
            subtasks.filter { subtask -> !subtask.isCompleted }.map { ListItem.SubtaskItem(it) }
        val eventItems = events.map { ListItem.EventItem(it) }
        val birthdayItems = birthdays.map { ListItem.BirthdayItem(it) }

        /** Mixes tasks and calendarItems into a single list
        to provide in Recycler View [ListAdapter] **/

        val mixedList = (taskItems + eventItems + birthdayItems + subtaskItems).sortedBy { item ->
            when (item) {
                is ListItem.TaskItem -> item.task.title
                is ListItem.EventItem -> item.event.summary
                is ListItem.BirthdayItem -> item.birthday.name
                is ListItem.SubtaskItem -> {
                    val parentTitle = tasks.find { it.id == item.subtask.parentId }?.title ?: ""
                    "$parentTitle / ${item.subtask.position}"
                }
            }
        }

        state.copy(
            items = mixedList,
            errorMessage = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListState()
    )

    fun toggleMenu() {
        _state.update { it.copy(isMenuExpanded = !it.isMenuExpanded) }
    }

    fun refreshData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            withContext(Dispatchers.IO) { syncAll() }
            _state.update { it.copy(isLoading = false) }
        }
    }

    suspend fun syncAll() = coroutineScope {
        val calendarStatus = async { calendarRepository.syncCalendar() }
        val tasksStatus = async { taskRepository.syncTasks() }

        calendarStatus.await()
        tasksStatus.await()
    }
}
