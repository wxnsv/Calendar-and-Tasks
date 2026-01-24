package com.nikkap.calendar.ui.main

import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.Task

sealed class ListItem {
    data class EventItem(val event: Event) : ListItem()
    data class TaskItem(val task: Task) : ListItem()
}