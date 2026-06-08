package com.nikkap.calendar.app.ui.screens.split.utils

import com.nikkap.calendar.core.utils.CalendarColors
import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task

sealed class SplitEntity {
    abstract val id: String
    abstract val date: Long
    abstract val title: String
    abstract val colorHex: String

    data class EventItem(val event: Event) : SplitEntity() {
        override val id: String = event.id!!
        override val date: Long = event.startTimestamp
        override val title: String = event.summary ?: "(No title)"
        override val colorHex: String = CalendarColors.getEventColor(event.colorId).hex
    }

    data class TaskItem(val task: Task) : SplitEntity() {
        override val id: String = task.id!!
        override val date: Long = task.deadline!!
        override val title: String = task.title ?: "(No title)"
        override val colorHex: String = CalendarColors.getTaskColor()
    }

    data class SubtaskItem(val subtask: Subtask, val deadline: Long? = null) : SplitEntity() {
        override val id: String = subtask.id
        override val date: Long = deadline ?: subtask.deadline!!
        override val title: String = subtask.title ?: "(No title)"
        override val colorHex: String = ""
    }

    data class BirthdayItem(val birthday: Birthday) : SplitEntity() {
        override val id: String = birthday.id!!
        override val date: Long = birthday.date!!
        override val title: String = birthday.name ?: "(No title)"
        override val colorHex: String = CalendarColors.getBirthdayColor(birthday.colorId).hex
    }
}