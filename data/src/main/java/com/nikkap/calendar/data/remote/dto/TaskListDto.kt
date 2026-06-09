package com.nikkap.calendar.data.remote.dto

data class TaskListDto(
    val id: String?,
    val title: String?,
    val updated: String? = null,
    val deleted: Boolean = false,
)