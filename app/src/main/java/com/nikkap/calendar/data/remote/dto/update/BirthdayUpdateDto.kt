package com.nikkap.calendar.data.remote.dto.update

import com.nikkap.calendar.data.remote.dto.BirthdayDateTime
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BirthdayUpdateDto(
    @field:Json(name = "id")
    val id: String? = null,
    @field:Json(name = "summary")
    val summary: String? = null,
    @field:Json(name = "start")
    val start: BirthdayDateTime? = null,
    @field:Json(name = "colorId")
    val colorId: String? = null,
)
