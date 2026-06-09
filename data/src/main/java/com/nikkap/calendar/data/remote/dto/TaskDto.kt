package com.nikkap.calendar.data.remote.dto

data class TaskDto(
    val id: String,
    val title: String?,
    val status: String? = null,
    val notes: String? = null,
    val due: String?,
    val parent: String? = null,
    val position: String? = null,
    val updated: String? = null,
    val deleted: Boolean = false,
)
