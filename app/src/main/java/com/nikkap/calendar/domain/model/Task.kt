package com.nikkap.calendar.domain.model

data class Task(
    val id: String,
    val title: String?,
    val notes: String? = null,
    val date: Long?,
    val isCompleted: Boolean,
    val updated: Long
)

