package com.nikkap.calendar.data.remote.dto

data class EventDto(
    val id: String,
    val summary: String?,
    val description: String?,
    val start: EventDateTime,
    val end: EventDateTime,
    val updated: String? = null,
    val deleted: Boolean = false,
    val colorId: String?,
    val status: String?,
)

data class EventDateTime(
    val dateTime: String?,
    val date: String?
)

