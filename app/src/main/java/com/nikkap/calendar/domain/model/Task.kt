package com.nikkap.calendar.domain.model

data class Task(
    val id: String = "",
    val title: String? = null,
    val notes: String? = null,
    val timestamp: Long = 0L,
    val deadline: Long? = null,
    val isCompleted: Boolean = false,
    val isAllDay: Boolean = false,
    val taskListId: String = "",
    val repeat: String? = null
) : CalendarEntry()

