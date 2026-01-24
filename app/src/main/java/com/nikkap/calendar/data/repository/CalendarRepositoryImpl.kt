package com.nikkap.calendar.data.repository

import com.nikkap.calendar.core.auth.AuthManager
import com.nikkap.calendar.data.remote.api.CalendarApi
import com.nikkap.calendar.data.remote.dto.toEvent
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.repository.CalendarRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

class CalendarRepositoryImpl(
    private val api: CalendarApi,
    private val authManager: AuthManager
) : CalendarRepository {
    override suspend fun getCalendarEvents(): List<Event> {
        val token = authManager.getAccessToken()
            ?: throw Exception("Не удалось получить токен")
        val timeMin = Clock.System.now()
            .minus(3, DateTimeUnit.YEAR, TimeZone.UTC)
            .toString()
        val response = api.getEvents(
            token = "Bearer $token",
            timeMin = timeMin
        )
        return response.body()?.items?.map { it.toEvent() } ?: emptyList()
    }
}