package com.nikkap.calendar.domain.model

import com.nikkap.calendar.core.utils.CalendarColors


data class Event(
    val id: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val startTimestamp: Long = 0L,
    val endTimestamp: Long = 0L,
    val isAllDay: Boolean = false,
    val colorHex: String? = CalendarColors.getEventColor(null).id,
    val status: EventStatus = EventStatus.CONFIRMED,
) : CalendarEntry()

enum class EventStatus { CONFIRMED, TENTATIVE, CANCELLED }

