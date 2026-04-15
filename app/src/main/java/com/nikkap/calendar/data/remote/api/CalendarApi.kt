package com.nikkap.calendar.data.remote.api

import com.nikkap.calendar.data.remote.dto.BirthdayDto
import com.nikkap.calendar.data.remote.dto.EventDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CalendarApi {
    @GET("calendar/v3/calendars/primary/events")
    suspend fun getEvents(
        @Query("timeMin") timeMin: String?,
        @Query("eventTypes") type: String = "default",
        @Query("updatedMin") updatedMin: String?,
        @Query("singleEvents") singleEvents: Boolean,
        @Query("showHidden") showHidden: Boolean = true,
        @Query("showDeleted") showDeleted: Boolean = true,
    ): Response<EventListResponse>

    @DELETE("calendar/v3/calendars/{calendarId}/events/{eventId}")
    suspend fun deleteItem(
        @Path("calendarId") calendarId: String = "primary",
        @Path("eventId") eventId: String
    ): Response<Unit>

    @GET("calendar/v3/calendars/primary/events")
    suspend fun getBirthdays(
        @Query("timeMin") timeMin: String?,
        @Query("updatedMin") updatedMin: String?,
        @Query("eventTypes") type: String = "birthday",
        @Query("showHidden") showHidden: Boolean = true,
        @Query("showDeleted") showDeleted: Boolean = true,
    ): Response<BirthdayListResponse>

    @POST("calendar/v3/calendars/primary/events")
    suspend fun createEvent(
        @Body event: EventDto
    ): Response<EventDto>

    @PATCH("calendar/v3/calendars/{calendarId}/events/{eventId}")
    suspend fun updateEvent(
        @Path("calendarId") calendarId: String = "primary",
        @Path("eventId") eventId: String,
        @Body event: EventDto
    ): Response<EventDto>

    @PATCH("calendar/v3/calendars/{calendarId}/events/{eventId}")
    suspend fun updateBirthday(
        @Path("calendarId") calendarId: String = "primary",
        @Path("eventId") birthdayId: String,
        @Body birthday: BirthdayDto
    ): Response<BirthdayDto>

    @POST("calendar/v3/calendars/primary/events")
    suspend fun createBirthday(
        @Body birthday: BirthdayDto
    ): Response<BirthdayDto>
}

data class EventListResponse(
    val items: List<EventDto>? = emptyList()
)

data class BirthdayListResponse(
    val items: List<BirthdayDto>? = emptyList()
)