package com.nikkap.calendar.domain.repository

import com.nikkap.calendar.domain.model.Event

interface CalendarRepository {
    suspend fun getCalendarEvents(): List<Event>
}