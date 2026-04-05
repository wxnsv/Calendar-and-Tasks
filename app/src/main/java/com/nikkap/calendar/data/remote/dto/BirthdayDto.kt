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
    val start: BirthdayItemDateTime,
    @field:Json(name = "updated")
    val updated: String = ""
)

data class BirthdayItemDateTime(
    @field:Json(name = "date")
    val date: String?
)