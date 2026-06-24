package com.nikkap.calendar.domain.repository

import com.nikkap.calendar.domain.model.Birthday
import com.nikkap.calendar.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    suspend fun syncCalendar(): Result<Unit>
    suspend fun getEvent(id: String): Event
    suspend fun getBirthday(id: String): Birthday
    fun getNonDeleteEvents(): Flow<List<Event>>
    fun getNonDeleteBirthdays(): Flow<List<Birthday>>
    suspend fun saveBirthday(birthday: Birthday)
    suspend fun saveEvent(event: Event)
    suspend fun updateBirthday(birthday: Birthday)
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(id: String)
    suspend fun deleteBirthday(id: String)
    fun clearAll()
}