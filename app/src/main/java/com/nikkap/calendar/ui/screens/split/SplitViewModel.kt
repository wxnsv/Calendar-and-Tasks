package com.nikkap.calendar.ui.screens.split

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import com.nikkap.calendar.ui.screens.split.utils.SplitEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class SplitViewModel(
    private val tasksRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SplitState())
    private val _tasksFlow = tasksRepository.allTasks
    private val _eventsFlow = calendarRepository.allEvents
    private val _birthdaysFlow = calendarRepository.allBirthdays
    private val _subtasksFlow = tasksRepository.allSubtasks
    val state: StateFlow<SplitState> = combine(
        _state,
        _tasksFlow,
        _eventsFlow,
        _birthdaysFlow,
        _subtasksFlow
    ) { state,
        tasks,
        events,
        birthdays,
        subtasks ->
        val taskItems =
            tasks.filter { task -> !task.isCompleted && task.deadline != null && task.deadline != 0L }
                .map { SplitEntity.TaskItem(it) }
        val subtaskItems =
            subtasks.map { subtask ->
                val tasks1 = tasks.filter { it.deadline != null && it.deadline != 0L }
                SplitEntity.SubtaskItem(
                    subtask,
                    deadline = tasks1.find { it.id == subtask.parentId }?.deadline
                )
            }.filter { subtask -> !subtask.subtask.isCompleted && subtask.deadline != null }
        val eventItems = events.map { SplitEntity.EventItem(it) }
        val birthdayItems = birthdays.map { SplitEntity.BirthdayItem(it) }

        /** Mixes taskItems and calendarItems into a single list **/

        val mixedList = (taskItems + eventItems + birthdayItems + subtaskItems).sortedBy { item ->
            when (item) {
                is SplitEntity.TaskItem -> item.task.title
                is SplitEntity.EventItem -> item.event.summary
                is SplitEntity.BirthdayItem -> item.birthday.name
                is SplitEntity.SubtaskItem -> {
                    val parentTitle = tasks.find { it.id == item.subtask.parentId }?.title ?: ""
                    "$parentTitle / ${item.subtask.position}"
                }
            }
        }

        state.copy(
            items = mixedList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SplitState()
    )

    fun onIntent(intent: SplitIntent) {
        when (intent) {
            is SplitIntent.UpdateSelectedDate -> {
                _state.update { it.copy(selectedDate = intent.date) }
            }
        }
    }

}