package com.nikkap.calendar.app.ui.screens.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.nikkap.calendar.data.worker.SyncWorker
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.repository.CalendarRepository
import com.nikkap.calendar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListViewModel(
    private val taskRepository: TaskRepository,
    private val calendarRepository: CalendarRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _tasksFlow = flow {
        val tasks = taskRepository.getNonDeleteTasks()
        emit(tasks)
    }
    private val _subtasksFlow = flow {
        val subtasks = taskRepository.getNonDeleteSubtasks()
        emit(subtasks)
    }
    private val _eventsFlow = flow {
        val events = calendarRepository.getNonDeleteEvents()
        emit(events)
    }
    private val _birthdaysFlow = flow {
        val birthdays = calendarRepository.getNonDeleteBirthdays()
        emit(birthdays)
    }
    private val _workInfoFlow = workManager
        .getWorkInfosForUniqueWorkFlow("ManualSyncWorker")
        .map { workInfoList ->
            workInfoList.firstOrNull()
        }
    private val _state = MutableStateFlow(ListState())
    val state: StateFlow<ListState> = combine(
        _state,
        _tasksFlow,
        _eventsFlow,
        _birthdaysFlow,
        _subtasksFlow,
        _workInfoFlow,
    ) { arrayOfFlows ->

        val state = arrayOfFlows[0] as ListState
        val tasks = arrayOfFlows[1] as List<Task>
        val events = arrayOfFlows[2] as List<Event>
        val birthdays = arrayOfFlows[3] as List<Birthday>
        val subtasks = arrayOfFlows[4] as List<Subtask>
        val workInfo = arrayOfFlows[5] as WorkInfo?


        val taskItems = tasks.filter { task -> !task.isCompleted }.map { ListItem.TaskItem(it) }
        val subtaskItems =
            subtasks.filter { subtask -> !subtask.isCompleted }.map { ListItem.SubtaskItem(it) }
        val eventItems = events.map { ListItem.EventItem(it) }
        val birthdayItems = birthdays.map { ListItem.BirthdayItem(it) }

        /** Mixes tasks and calendarItems into a single list
        to provide in Recycler View [ListAdapter] **/

        val sortedParents = (taskItems + eventItems + birthdayItems).sortedBy { item ->
            when (item) {
                is ListItem.TaskItem -> item.task.deadline ?: 0L

                is ListItem.EventItem -> item.event.startTimestamp
                is ListItem.BirthdayItem -> item.birthday.date ?: Long.MAX_VALUE
                else -> Long.MAX_VALUE
            }
        }

        val mixedList = mutableListOf<ListItem>()

        for (parent in sortedParents) {
            mixedList.add(parent)

            if (parent is ListItem.TaskItem) {
                val associatedSubtasks = subtaskItems
                    .filter { it.subtask.parentId == parent.task.id }
                    .sortedBy { it.subtask.position }

                mixedList.addAll(associatedSubtasks)
            }
        }
        val isRefreshing =
            workInfo?.state == WorkInfo.State.RUNNING || workInfo?.state == WorkInfo.State.ENQUEUED

        state.copy(
            items = mixedList,
            isRefreshing = isRefreshing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListState()
    )

    fun refreshData() {
            _state.update { it.copy(isRefreshing = true) }
        syncAll() // TODO 4/5
    }

    private fun syncAll() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .build()
        workManager.enqueueUniqueWork(
            "ManualSyncWorker",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(syncRequest.id).collect { workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.FAILED) {
                    val errorKey = workInfo.outputData.getString("SYNC_ERROR_MESSAGE")
                    _state.update { it.copy(errorMessage = errorKey) }
                }
            }
        }
    }
}
