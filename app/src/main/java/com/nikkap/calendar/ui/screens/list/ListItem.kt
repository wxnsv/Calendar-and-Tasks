package com.nikkap.calendar.ui.screens.list

import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task

sealed class ListItem {
    abstract val id: String

    data class EventItem(val event: Event) : ListItem() {
        override val id: String = event.id!!
    }

    data class TaskItem(val task: Task) : ListItem() {
        override val id: String = task.id!!
    }

    data class SubtaskItem(val subtask: Subtask) : ListItem() {
        override val id: String = subtask.id
    }

    data class BirthdayItem(val birthday: Birthday) : ListItem() {
        override val id: String = birthday.id!!
    }
}