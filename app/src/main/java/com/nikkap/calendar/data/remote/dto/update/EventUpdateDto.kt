package com.nikkap.calendar.data.remote.dto.update

import com.nikkap.calendar.data.remote.dto.EventDateTime
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventUpdateDto(
    @field:Json(name = "id")
    val id: String? = null,
    @field:Json(name = "summary")
    val summary: String? = null,
    @field:Json(name = "description")
    val description: String? = null,
    @field:Json(name = "start")
    val start: EventDateTime? = null,
    @field:Json(name = "end")
    val end: EventDateTime? = null,
    @field:Json(name = "colorId")
    val colorId: String? = null,
    @field:Json(name = "status")
    val status: String? = null,
    @field:Json(name = "updated")
    val updated: String? = null,
)
