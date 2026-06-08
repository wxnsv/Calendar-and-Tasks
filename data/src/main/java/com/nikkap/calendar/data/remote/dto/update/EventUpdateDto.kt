package com.nikkap.calendar.data.remote.dto.update

import com.nikkap.calendar.data.remote.dto.EventDateTime

data class EventUpdateDto(
    val id: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val start: EventDateTime? = null,
    val end: EventDateTime? = null,
    val colorId: String? = null,
    val status: String? = null,
    val updated: String? = null,
)
