package com.nikkap.calendar.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskListDto(
    @field:Json(name = "id")
    val id: String,
    @field:Json(name = "title")
    val title: String
)