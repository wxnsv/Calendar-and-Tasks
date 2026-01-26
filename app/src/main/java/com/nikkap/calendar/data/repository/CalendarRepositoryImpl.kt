package com.nikkap.calendar.data.repository

import com.nikkap.calendar.core.auth.AuthManager
import com.nikkap.calendar.data.local.dao.CalendarDao
import com.nikkap.calendar.data.mapper.toCalendarEntity
import com.nikkap.calendar.data.mapper.toCalendarItem
import com.nikkap.calendar.data.remote.api.CalendarApi
import com.nikkap.calendar.domain.model.CalendarItem
import com.nikkap.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

class CalendarRepositoryImpl(
    private val api: CalendarApi,
    private val authManager: AuthManager,
    private val dao: CalendarDao
) : CalendarRepository {
    override val allItems: Flow<List<CalendarItem>> = dao.getAllItems()
        .map { entities ->
            entities.map { it.toCalendarItem() }
        }

    override suspend fun haveLocalData(): Boolean {
        return dao.getCount() > 0
    }

    override suspend fun syncCalendar(): Result<Unit> {
        return try {
            val token = authManager.getAccessToken()
            val timeMin = Clock.System.now()
                .minus(3, DateTimeUnit.YEAR, TimeZone.UTC)
                .toString()
            val response = api.getCalendarItems(
                token = "Bearer $token",
                timeMin = timeMin
            )

            if (response.isSuccessful) {
                val entities = response.body()?.items?.map { it.toCalendarEntity() } ?: emptyList()
                dao.insertItems(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}