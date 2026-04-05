package com.nikkap.calendar.domain.model

data class Subtask(
    val id: String,
    val title: String?,
    val parentId: String,
    val position: String,
    val isCompleted: Boolean,
    val taskListId: String,
)
