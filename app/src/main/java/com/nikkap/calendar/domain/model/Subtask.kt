package com.nikkap.calendar.domain.model

data class Subtask(
    val id: String = "",
    val title: String? = "",
    val parentId: String = "",
    val deadline: Long? = null,
    val position: String = "",
    val isCompleted: Boolean = false,
    val taskListId: String = "",
) : CalendarEntry()
