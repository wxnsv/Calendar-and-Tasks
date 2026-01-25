package com.nikkap.calendar.domain.repository

import com.nikkap.calendar.domain.model.CalendarItem

interface CalendarRepository {
    suspend fun getCalendarItems(): List<CalendarItem>
}