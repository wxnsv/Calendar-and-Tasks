package com.nikkap.calendar.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.core.utils.toTimeLong
import com.nikkap.calendar.data.remote.dto.BirthdayItemDateTime
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.CalendarEntry
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CreateViewModel(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val taskListsFlow = taskRepository.allTaskLists
    private val _state = MutableStateFlow(CreateState.initial())
    val state: StateFlow<CreateState> = combine(
        _state,
        taskListsFlow
    ) { state, taskLists ->
        val defaultList = taskLists.firstOrNull()

        state.copy(
            taskLists = taskLists,
            selectedTaskList = state.selectedTaskList ?: defaultList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateState()
    )

    fun onIntent(intent: CreateIntent) {
        when (intent) {
            is CreateIntent.UpdateCreateType -> {


            }

            is CreateIntent.UpdateTitle -> {
                _state.update { it.copy(title = intent.title) }
            }

            is CreateIntent.UpdateItem -> {
                var intentEntry: CalendarEntry
                when (intent.type) {
                    "TASK" -> intentEntry = Task()
                    "EVENT" -> intentEntry = Event()
                    else -> intentEntry = Birthday()
                }
                when (intentEntry) {
                    is Task -> viewModelScope.launch {
                        val task = taskRepository.getTask(intent.id)
                        _state.update {
                            it.copy(
                                taskDraft = task,
                                title = task.title
                            )
                        }
                    }

                    is Birthday -> viewModelScope.launch {
                        val birthday = calendarRepository.getBirthday(
                            intent.id
                        )
                        _state.update {
                            it.copy(
                                birthdayDraft = birthday,
                                title = birthday.name
                            )
                        }
                    }

                    is Event -> viewModelScope.launch {
                        val event = calendarRepository.getEvent(intent.id)
                        _state.update {
                            it.copy(
                                eventDraft = event,
                                eventStartTime = event.startTimestamp.toTimeLong(),
                                eventEndTime = event.endTimestamp.toTimeLong(),
                                title = event.summary
                            )
                        }
                    }
                }

            }
        }
    }

    fun onTaskIntent(intent: CreateTaskIntent) {
        when (intent) {
            is CreateTaskIntent.UpdateDescription -> _state.update {
                it.copy(
                    taskDraft = it.taskDraft.copy(
                        notes = intent.description
                    )
                )
            }

            is CreateTaskIntent.UpdateIsAllDay -> _state.update {
                it.copy(
                    taskDraft = it.taskDraft.copy(
                        isAllDay = intent.isAllDay
                    )
                )
            }

            is CreateTaskIntent.UpdateDeadline -> _state.update {
                it.copy(
                    taskDraft = it.taskDraft.copy(
                        deadline = intent.deadline
                    )
                )
            }

            is CreateTaskIntent.SaveTask -> {
                viewModelScope.launch {
                    val task = state.value.taskDraft.copy(
                        id = UUID.randomUUID().toString().replace("-", ""),
                        title = state.value.title,
                        timestamp = state.value.taskDraft.timestamp.plus(state.value.taskTime)
                    )
                    _state.update { it.copy(isLoading = true) }
                    taskRepository.saveTask(task)
                    _state.update { it.copy(isLoading = false) }
                }
            }

            is CreateTaskIntent.UpdateList -> {
                _state.update { it.copy(selectedTaskList = intent.taskList) }
            }

            is CreateTaskIntent.UpdateRepeat -> {
                _state.update {
                    it.copy(
                        taskDraft = it.taskDraft.copy(
                            repeat = intent.repeat
                        )
                    )
                }
            }

            is CreateTaskIntent.UpdateTime -> {
                _state.update {
                    it.copy(
                        taskTime = intent.time
                    )
                }
            }

            is CreateTaskIntent.UpdateDate -> {
                _state.update {
                    it.copy(
                        taskDraft = it.taskDraft.copy(
                            timestamp = intent.date
                        )
                    )
                }
            }
        }
    }

    fun onEventIntent(intent: CreateEventIntent) {
        when (intent) {
            CreateEventIntent.SaveEvent -> {
                viewModelScope.launch {
                    val event = state.value.eventDraft
                    val saveEvent = event.copy(
                        id = event.id ?: UUID.randomUUID().toString().replace("-", ""),
                        summary = state.value.title,
                        startTimestamp = state.value.eventDraft.startTimestamp + state.value.eventStartTime,
                        endTimestamp = state.value.eventDraft.endTimestamp + state.value.eventEndTime
                    )
                    _state.update { it.copy(isLoading = true) }
                    calendarRepository.saveEvent(saveEvent)
                    _state.update { it.copy(isLoading = false) }
                }
            }

            is CreateEventIntent.UpdateColor ->
                _state.update {
                    it.copy(
                        eventDraft = it.eventDraft.copy(
                            colorHex = intent.color.toString()
                        )
                    )
                }

            is CreateEventIntent.UpdateDescription ->
                _state.update {
                    it.copy(
                        eventDraft = it.eventDraft.copy(
                            description = intent.description
                        )
                    )
                }

            is CreateEventIntent.UpdateIsAllDay ->
                _state.update {
                    it.copy(
                        eventDraft = it.eventDraft.copy(
                            isAllDay = intent.isAllDay
                        )
                    )
                }

            is CreateEventIntent.UpdateStartDate -> _state.update {
                it.copy(
                    eventDraft = it.eventDraft.copy(
                        startTimestamp = intent.startDate
                    )
                )
            }

            is CreateEventIntent.UpdateEndDate -> _state.update {
                it.copy(
                    eventDraft = it.eventDraft.copy(
                        endTimestamp = intent.endDate
                    )
                )
            }

            is CreateEventIntent.UpdateEndTime -> {
                _state.update {
                    it.copy(
                        eventEndTime = intent.endTime
                    )
                }
            }

            is CreateEventIntent.UpdateStartTime -> {
                _state.update {
                    it.copy(
                        eventStartTime = intent.startTime
                    )
                }
            }
        }
    }

    fun onBirthdayIntent(intent: CreateBirthdayIntent) {
        when (intent) {
            is CreateBirthdayIntent.UpdateColor -> _state.update {
                it.copy(
                    birthdayDraft = it.birthdayDraft.copy(
                        colorHex = intent.color
                    )
                )
            }

            is CreateBirthdayIntent.UpdateDate -> _state.update {
                it.copy(
                    birthdayDraft = it.birthdayDraft.copy(
                        date = BirthdayItemDateTime(date = intent.date.toString())
                    )
                )
            }

            CreateBirthdayIntent.SaveBirthday -> {
                viewModelScope.launch {
                    val birthday = _state.value.birthdayDraft.copy(name = state.value.title)
                    _state.update { it.copy(isLoading = true) }
                    calendarRepository.saveBirthday(birthday)
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
