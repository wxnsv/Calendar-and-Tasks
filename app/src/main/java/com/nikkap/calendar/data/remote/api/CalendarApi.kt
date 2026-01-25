package com.nikkap.calendar.data.remote.api

import com.nikkap.calendar.data.remote.dto.CalendarItemDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CalendarApi {
    @GET("calendar/v3/calendars/primary/events")
    suspend fun getCalendarItems(
        @Header("Authorization") token: String,
        @Query("timeMin") timeMin: String
    ): Response<CalendarItemListResponse>
}

data class CalendarItemListResponse(
    val items: List<CalendarItemDto>? = emptyList()
)