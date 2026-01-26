package com.nikkap.calendar.domain.repository

import com.nikkap.calendar.domain.model.CalendarItem
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    suspend fun syncCalendar(): Result<Unit>
    suspend fun haveLocalData(): Boolean
    val allItems: Flow<List<CalendarItem>>
}