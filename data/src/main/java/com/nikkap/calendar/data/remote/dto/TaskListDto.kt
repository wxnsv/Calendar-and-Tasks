package com.nikkap.calendar.data.remote.dto

data class TaskListDto(
    val id: String,
    val title: String,
    val updated: String,
    val deleted: Boolean = false,
)