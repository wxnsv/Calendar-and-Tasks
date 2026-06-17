package com.nikkap.calendar.app.ui.screens.create

import android.icu.util.Calendar
import com.nikkap.calendar.core.utils.toOnlyDateLong
import com.nikkap.calendar.core.utils.toTimeLong
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.model.TaskList

data class CreateState(
    val title: String? = "",
    val activeType: String = "TASK",

    val taskDraft: Task = Task(),
    val eventDraft: Event = Event(),
    val birthdayDraft: Birthday = Birthday(),

    val subtasks: List<Subtask> = emptyList(),

    val isLoading: Boolean = false,
    val isEditing: Boolean = false,

    val taskLists: List<TaskList> = emptyList(),
    val selectedTaskList: TaskList? = null,

    val eventStartTime: Long = 0L,
    val eventEndTime: Long = 0L,
    val eventStartDate: Long = 0L,
    val eventEndDate: Long = 0L,

    val birthdayDate: Long = 0L
) {
    companion object {
        fun initial(): CreateState {
            val startTimestamp = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endTimestamp = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 2)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val eventStartDate = Calendar.getInstance().timeInMillis.toOnlyDateLong()

            val eventEndDate = Calendar.getInstance().timeInMillis.toOnlyDateLong()

            val birthdayStartDate = Calendar.getInstance().timeInMillis.toOnlyDateLong()

            return CreateState(
                eventStartTime = startTimestamp.toTimeLong(),
                eventEndTime = endTimestamp.toTimeLong(),

                eventStartDate = eventStartDate,
                eventEndDate = eventEndDate,
                taskDraft = Task(deadline = startTimestamp),

                birthdayDate = birthdayStartDate
            )
        }
    }
}