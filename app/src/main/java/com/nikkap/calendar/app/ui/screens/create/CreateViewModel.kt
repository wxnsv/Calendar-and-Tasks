package com.nikkap.calendar.app.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.core.utils.toOnlyDateLong
import com.nikkap.calendar.core.utils.toTimeLong
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.CalendarEntry
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.EventStatus
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.util.UUID

class CreateViewModel(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val userPrefRepository: UserPreferencesRepository
) : ViewModel() {
    private val _taskListsFlow = taskRepository.getNonDeleteTaskLists()
    private val _state = MutableStateFlow(CreateState.initial())
    val state: StateFlow<CreateState> = combine(
        _state,
        _taskListsFlow
    ) { state, taskLists ->

        state.copy(
            taskLists = taskLists,
            selectedTaskList = state.selectedTaskList
                ?: taskLists.find { it.id == state.taskDraft.taskListId }
                ?: taskLists.find { it.id == userPrefRepository.defaultTasklistId.first() },
            eventDraft = state.eventDraft.copy(
                startTimestamp = if (!state.eventDraft.isAllDay) state.eventStartDate + state.eventStartTime else state.eventStartDate,
                endTimestamp = if (!state.eventDraft.isAllDay) state.eventEndDate + state.eventEndTime else state.eventEndDate
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateState()
    )

    private val _errorEvents = Channel<String>()
    val errorEvents = _errorEvents.receiveAsFlow()

    fun onIntent(intent: CreateIntent) {
        when (intent) {
            is CreateIntent.UpdateShowFragment -> {
                _state.update { it.copy(activeType = intent.type) }
            }

            is CreateIntent.UpdateTitle -> {
                _state.update { it.copy(title = intent.title) }
            }

            is CreateIntent.UpdateItem -> {
                val intentEntry: CalendarEntry = when (intent.type) {
                    "TASK" -> Task()
                    "EVENT" -> Event()
                    else -> Birthday()
                }
                if (intent.id.isNullOrBlank()) return
                _state.update { it.copy(isEditing = true) }
                when (intentEntry) {
                    is Task -> viewModelScope.launch {
                        val task = taskRepository.getTask(intent.id)
                        val subtasks = taskRepository.getSubtasksByParentId(parentId = intent.id)
                        _state.update {
                            it.copy(
                                taskDraft = task,
                                title = task.title,
                                isLoading = false,
                                subtasks = subtasks
                            )
                        }
                    }

                    is Birthday -> viewModelScope.launch {
                        val birthday = calendarRepository.getBirthday(intent.id)
                        _state.update {
                            it.copy(
                                birthdayDraft = birthday,
                                title = birthday.name,
                                isLoading = false
                            )
                        }
                    }

                    is Event -> viewModelScope.launch {
                        val event = calendarRepository.getEvent(intent.id)
                        _state.update {
                            it.copy(
                                eventDraft = event,
                                eventStartDate = event.startTimestamp.toOnlyDateLong(),
                                eventEndDate = event.endTimestamp.toOnlyDateLong(),
                                eventStartTime = event.startTimestamp.toTimeLong(),
                                eventEndTime = event.endTimestamp.toTimeLong(),
                                title = event.summary,
                                isLoading = false
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

            is CreateTaskIntent.UpdateDeadline -> _state.update {
                it.copy(
                    taskDraft = it.taskDraft.copy(
                        deadline = intent.deadline
                    )
                )
            }

            is CreateTaskIntent.SaveTask -> {
                viewModelScope.launch {
                    val state = state.value
                    val task = state.taskDraft

                    val taskToSave = task.copy(
                        id = task.id ?: UUID.randomUUID().toString().replace("-", ""),
                        title = state.title,
                        taskListId = state.selectedTaskList!!.id
                    )
                    val subtasksToSave = state.subtasks.map {
                        it.copy(
                            parentId = taskToSave.id!!,
                            taskListId = taskToSave.taskListId
                        )
                    }

                    _state.update { it.copy(isLoading = true) }

                    if (!state.taskLists.contains(state.selectedTaskList))
                        launch {
                            taskRepository.saveTasklist(
                                state.selectedTaskList
                            )
                        }

                    if (state.isEditing) {
                        val subtasksBeforeSave =
                            taskRepository.getSubtasksByParentId(taskToSave.id!!)

                        val subtasksToSaveIdMap = subtasksToSave.associateBy { it.id }
                        val subtasksBeforeSaveIdMap = subtasksBeforeSave.associateBy { it.id }

                        val (insertSubtasks, updateSubtasks) = subtasksToSave.partition { subtaskToSave ->
                            val subtaskBeforeSave = subtasksBeforeSaveIdMap[subtaskToSave.id]
                            subtaskBeforeSave == null
                        }

                        val deleteSubtasks = subtasksBeforeSave.filter { subtaskBeforeSave ->
                            val subtaskToSave = subtasksToSaveIdMap[subtaskBeforeSave.id]
                            subtaskToSave == null
                        }

                        if (insertSubtasks.isNotEmpty()) taskRepository.saveSubtasks(insertSubtasks)
                        if (updateSubtasks.isNotEmpty()) taskRepository.updateSubtasks(
                            updateSubtasks
                        )
                        if (deleteSubtasks.isNotEmpty()) deleteSubtasks.forEach { deleteSubtask ->
                            taskRepository.deleteSubtask(deleteSubtask.id)
                        }
                        taskRepository.updateTask(taskToSave)

                    } else {
                        taskRepository.saveTask(taskToSave)
                        if (subtasksToSave.isNotEmpty()) taskRepository.saveSubtasks(subtasksToSave)
                    }
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

            is CreateTaskIntent.AddSubtask -> {
                _state.update {
                    val maxSubtasksPosition = state.value.subtasks.maxOfOrNull { it.position }
                    val newSubtaskPosition = if (!maxSubtasksPosition.isNullOrBlank()) {
                        try {
                            val bigNum = BigInteger(maxSubtasksPosition.trim())

                            val nextNum = bigNum.add(BigInteger.ONE)

                            nextNum.toString().padStart(maxSubtasksPosition.length, '0')
                        } catch (e: Exception) {
                            "00000000000000000000"
                        }
                    } else {
                        "00000000000000000000"
                    }
                    val subtask = Subtask(
                        id = UUID.randomUUID().toString().replace("-", ""),
                        title = intent.title,
                        position = newSubtaskPosition
                    )
                    it.copy(
                        subtasks = it.subtasks + subtask
                    )
                }
            }

            is CreateTaskIntent.DeleteSubtask -> {
                _state.update {
                    val deleteSubtask = it.subtasks.first {
                        it.id == intent.id
                    }
                    it.copy(
                        subtasks = it.subtasks - deleteSubtask
                    )
                }
            }

            is CreateTaskIntent.UpdateCompleteSubtask -> _state.update {
                val updatedList = it.subtasks.map { subtask ->
                    if (subtask.id == intent.id) subtask.copy(isCompleted = intent.isCompleted) else subtask
                }
                it.copy(
                    subtasks = updatedList
                )
            }

            is CreateTaskIntent.UpdateTitleSubtask -> _state.update {
                val subtasks = it.subtasks.toMutableList()
                val oldSubtask = subtasks.filter {
                    it.id == intent.id
                }.first()
                val updatedSubtask = oldSubtask.copy(title = intent.title)
                val index = subtasks.indexOfFirst { it.id == intent.id }
                subtasks[index] = updatedSubtask
                it.copy(
                    subtasks = subtasks
                )
            }
        }
    }

    fun onEventIntent(intent: CreateEventIntent) {
        when (intent) {
            CreateEventIntent.SaveEvent -> {
                viewModelScope.launch {
                    val event = state.value.eventDraft

                    val eventToSave = event.copy(
                        id = event.id ?: UUID.randomUUID().toString().replace("-", ""),
                        summary = state.value.title,
                        startTimestamp = event.startTimestamp,
                        endTimestamp = event.endTimestamp,
                        status = EventStatus.CONFIRMED
                    )
                    _state.update { it.copy(isLoading = true) }
                    if (state.value.isEditing) calendarRepository.updateEvent(eventToSave)
                    else calendarRepository.saveEvent(eventToSave)
                    _state.update { it.copy(isLoading = false) }

                }
            }

            is CreateEventIntent.UpdateColor ->
                _state.update {
                    it.copy(
                        eventDraft = it.eventDraft.copy(
                            colorId = intent.color
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

            is CreateEventIntent.UpdateStartDate ->
                _state.update {
                    it.copy(
                        eventStartDate = intent.startDate
                    )
                }

            is CreateEventIntent.UpdateEndDate ->
                _state.update {
                    it.copy(
                        eventEndDate = intent.endDate
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
                        colorId = intent.color
                    )
                )
            }

            is CreateBirthdayIntent.UpdateDate -> _state.update {
                it.copy(
                    birthdayDate = intent.date
                )
            }

            CreateBirthdayIntent.SaveBirthday -> {
                viewModelScope.launch {
                    val birthday = state.value.birthdayDraft
                    val birthdayToSave = birthday.copy(
                        id = birthday.id ?: UUID.randomUUID().toString().replace("-", ""),
                        name = state.value.title,
                        date = state.value.birthdayDate
                    )
                    _state.update { it.copy(isLoading = true) }
                    if (state.value.isEditing) calendarRepository.updateBirthday(birthdayToSave)
                    else calendarRepository.saveBirthday(birthdayToSave)
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun checkSubtaskBeforeSave(text: String): Result<Unit> {
        if (text == "") {
            viewModelScope.launch {
                _errorEvents.send("Subtask title cannot be empty")
            }
            return Result.failure(Exception("Subtask title cannot be empty"))
        }
        if (text.length > 1024) {
            viewModelScope.launch {
                _errorEvents.send("Subtask title is too long")
            }
            return Result.failure(Exception("Subtask title is too long"))
        } else return Result.success(Unit)
    }

    fun checkItemAndSave(): Result<Unit> {
        val state = state.value
        when (state.activeType) {
            "TASK" -> {
                val task = state.taskDraft
                if (state.title == null || state.title == "") {
                    viewModelScope.launch {
                        _errorEvents.send("Title cant be empty")
                    }
                    return Result.failure(Exception("Title cant be empty"))
                }
                if (state.title.length > 1024) {
                    viewModelScope.launch {
                        _errorEvents.send("Title is too long")
                    }
                    return Result.failure(Exception("Title is too long"))
                }
                if ((task.notes?.length ?: 0) > 8192) {
                    viewModelScope.launch {
                        _errorEvents.send("Description is too long")
                    }
                    return Result.failure(Exception("Description is too long"))
                } else {
                    onTaskIntent(CreateTaskIntent.SaveTask)
                }
            }

            "EVENT" -> {
                val event = state.eventDraft
                if (state.title == null || state.title == "") {
                    viewModelScope.launch {
                        _errorEvents.send("Title cant be empty")
                    }
                    return Result.failure(Exception("Title cant be empty"))
                }
                if (state.title.length > 1024) {
                    viewModelScope.launch {
                        _errorEvents.send("Title is too long")
                    }
                    return Result.failure(Exception("Title is too long"))
                }
                if ((event.description?.length ?: 0) > 8192) {
                    viewModelScope.launch {
                        _errorEvents.send("Description is too long")
                    }
                    return Result.failure(Exception("Description is too long"))
                }
                if (!event.isAllDay && event.startTimestamp + state.eventStartTime > event.endTimestamp + state.eventEndTime) {
                    viewModelScope.launch {
                        _errorEvents.send("Event start cant be later than end")
                    }
                    return Result.failure(Exception("Event start cant be later than end"))
                } else {
                    onEventIntent(CreateEventIntent.SaveEvent)
                }
            }

            "BIRTHDAY" -> {
                if (state.title == null || state.title == "") {
                    viewModelScope.launch {
                        _errorEvents.send("Name cant be empty")
                    }
                    return Result.failure(Exception("Name cant be empty"))
                }
                if (state.title.length > 1024) {
                    viewModelScope.launch {
                        _errorEvents.send("Name is too long")
                    }
                    return Result.failure(Exception("Name is too long"))
                } else {
                    onBirthdayIntent(CreateBirthdayIntent.SaveBirthday)
                }
            }
        }
        return Result.success(Unit)
    }
}
