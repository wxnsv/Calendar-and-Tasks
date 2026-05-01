package com.nikkap.calendar.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BirthdayDto(
    @field:Json(name = "id")
    val id: String,
    @field:Json(name = "summary")
    val summary: String?,
    @field:Json(name = "start")
    val start: BirthdayDateTime,
    @field:Json(name = "updated")
    val updated: String = "",
    @field:Json(name = "deleted")
    val deleted: Boolean = false,
    @field:Json(name = "colorId")
    val colorId: String?,
)

data class BirthdayDateTime(
    @field:Json(name = "date")
    val date: String?
)