package com.nikkap.calendar.data.repository

import android.util.Log
import com.nikkap.calendar.core.auth.AuthManager
import com.nikkap.calendar.data.mapper.toCalendarItem
import com.nikkap.calendar.data.remote.api.CalendarApi
import com.nikkap.calendar.domain.model.CalendarItem
import com.nikkap.calendar.domain.repository.CalendarRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

class CalendarRepositoryImpl(
    private val api: CalendarApi,
    private val authManager: AuthManager
) : CalendarRepository {
    override suspend fun getCalendarItems(): List<CalendarItem> {
        val token = authManager.getAccessToken()
            ?: throw Exception("Не удалось получить токен")
        Log.d("Network", "token = $token")
        val timeMin = Clock.System.now()
            .minus(3, DateTimeUnit.YEAR, TimeZone.UTC)
            .toString()
        val response = api.getCalendarItems(
            token = "Bearer $token",
            timeMin = timeMin
        )
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            Log.e("Network", "Code: ${response.code()}, Message: $errorBody")
        }
        Log.d("Network", "response.isSuccessful = ${response.isSuccessful}")
        return response.body()?.items?.map { it.toCalendarItem() } ?: emptyList()
    }
}