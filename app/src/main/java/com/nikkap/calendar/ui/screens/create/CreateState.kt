package com.nikkap.calendar.ui.screens.create

import android.icu.util.Calendar
import com.nikkap.calendar.core.utils.toTimeLong
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.CalendarEntry
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.model.TaskList

data class CreateState(
    val title: String? = "",
    val activeType: CalendarEntry = Task(),

    val taskDraft: Task = Task(),
    val eventDraft: Event = Event(),
    val birthdayDraft: Birthday = Birthday(),

    val isLoading: Boolean = false,
    val isEditing: Boolean = false,

    val taskLists: List<TaskList> = emptyList(),
    val selectedTaskList: TaskList? = null,

    val eventStartTime: Long = 0L,
    val eventEndTime: Long = 0L,

    val taskStartTime: Long = 0L,
    val taskTime: Long = 0L
) {
    companion object {
        fun initial(): CreateState {
            val startTimestamp = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
            }.timeInMillis

            val endTimestamp = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 2)
                set(Calendar.MINUTE, 0)
            }.timeInMillis

            return CreateState(
                eventStartTime = startTimestamp.toTimeLong(),
                eventEndTime = endTimestamp.toTimeLong(),
                taskTime = startTimestamp.toTimeLong(),

                eventDraft = Event(startTimestamp = startTimestamp, endTimestamp = endTimestamp),
                taskDraft = Task(timestamp = startTimestamp)
            )
        }
    }
}

sealed class ShowFragment {
    object Task : ShowFragment()
    object Event : ShowFragment()
    object Birthday : ShowFragment()
}