package com.nikkap.calendar.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventDto(
    @field:Json(name = "id")
    val id: String,
    @field:Json(name = "summary")
    val summary: String?,
    @field:Json(name = "description")
    val description: String?,
    @field:Json(name = "start")
    val start: CalendarItemDateTime,
    @field:Json(name = "end")
    val end: CalendarItemDateTime,
    @field:Json(name = "updated")
    val updated: String?,
    @field:Json(name = "deleted")
    val deleted: Boolean = false,
    @field:Json(name = "colorId")
    val colorId: String?,
    @field:Json(name = "status")
    val status: String?,
)

data class CalendarItemDateTime(
    @field:Json(name = "dateTime")
    val dateTime: String?,
    @field:Json(name = "date")
    val date: String?
)

