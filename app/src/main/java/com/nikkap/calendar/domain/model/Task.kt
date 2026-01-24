package com.nikkap.calendar.domain.model

data class Task(
    val id: String,
    val title: String,
    val status: String? = null,
    val notes: String? = null
)
