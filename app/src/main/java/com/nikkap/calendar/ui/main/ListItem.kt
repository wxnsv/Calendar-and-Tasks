package com.nikkap.calendar.ui.main

import com.nikkap.calendar.domain.model.CalendarItem
import com.nikkap.calendar.domain.model.Task

sealed class ListItem {
    data class EventItem(val calendarItem: CalendarItem) : ListItem()
    data class TaskItem(val task: Task) : ListItem()
}