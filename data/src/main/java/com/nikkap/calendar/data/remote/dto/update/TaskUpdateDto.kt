package com.nikkap.calendar.data.remote.dto.update

data class TaskUpdateDto(
    val id: String? = null,
    val title: String? = null,
    val status: String? = null,
    val notes: String? = null,
    val due: String? = null,
    val parent: String? = null,
    val position: String? = null,
    val updated: String? = null,
)
