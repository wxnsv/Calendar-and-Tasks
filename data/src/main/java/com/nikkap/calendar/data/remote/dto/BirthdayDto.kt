package com.nikkap.calendar.data.remote.dto

data class BirthdayDto(
    val id: String,
    val summary: String?,
    val start: BirthdayDateTime,
    val end: BirthdayDateTime,
    val updated: String? = null,
    val deleted: Boolean = false,
    val colorId: String?,
    val eventType: String = "birthday"
)

data class BirthdayDateTime(
    val date: String?
)