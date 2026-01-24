package com.nikkap.calendar.data.remote.api

import com.nikkap.calendar.data.remote.dto.EventDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CalendarApi {
    @GET("calendar/v3/calendars/primary/events")
    suspend fun getEvents(
        @Header("Authorization") token: String,
        @Query("timeMin") timeMin: String
    ): Response<CalendarEventListResponse>
}

data class CalendarEventListResponse(
    val items: List<EventDto>? = emptyList()
)