package com.nikkap.calendar.domain.repository

import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    suspend fun syncCalendar(): Result<Unit>
    suspend fun haveLocalData(): Boolean
    val allEvents: Flow<List<Event>>
    val allBirthdays: Flow<List<Birthday>>
    suspend fun getEvent(id: String): Event
    suspend fun getBirthday(id: String): Birthday
    suspend fun saveBirthday(birthday: Birthday)
    suspend fun saveEvent(event: Event)
    suspend fun updateBirthday(birthday: Birthday)
    suspend fun updateEvent(event: Event)
}