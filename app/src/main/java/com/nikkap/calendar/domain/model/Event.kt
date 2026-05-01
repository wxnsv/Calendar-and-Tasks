package com.nikkap.calendar.domain.model

data class Event(
    val id: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val startTimestamp: Long = 0L,
    val endTimestamp: Long = 0L,
    val isAllDay: Boolean = false,
    val colorId: Int? = null,
    val status: EventStatus? = null,
) : CalendarEntry()

enum class EventStatus { CONFIRMED, TENTATIVE, CANCELLED }

