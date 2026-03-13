package com.nikkap.calendar.data.remote.api

import com.nikkap.calendar.data.remote.dto.BirthdayDto
import com.nikkap.calendar.data.remote.dto.EventDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CalendarApi {
    @GET("calendar/v3/calendars/primary/events")
    suspend fun getEvents(
        @Query("timeMin") timeMin: String,
        @Query("eventTypes") type: String,
        @Query("singleEvents") singleEvents: Boolean,
    ): Response<EventListResponse>

    @GET("calendar/v3/calendars/primary/events")
    suspend fun getBirthdays(
        @Query("timeMin") timeMin: String,
        @Query("eventTypes") type: String,
    ): Response<BirthdayListResponse>

    @POST("https://www.googleapis.com/calendar/v3/calendars/primary/events")
    suspend fun createEvent(
        @Body event: EventDto
    ): Response<Unit>

    @PATCH("calendar/v3/calendars/{calendarId}/events/{eventId}")
    suspend fun updateEvent(
        @Path("calendarId") calendarId: String = "primary",
        @Path("eventId") eventId: String,
        @Body event: EventDto
    ): Response<Unit>

    @POST("calendar/v3/calendars/primary/events")
    suspend fun createBirthday(
        @Body birthday: BirthdayDto
    ): Response<Unit>
}

data class EventListResponse(
    val items: List<EventDto>? = emptyList()
)

data class BirthdayListResponse(
    val items: List<BirthdayDto>? = emptyList()
)