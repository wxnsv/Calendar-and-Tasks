package com.nikkap.calendar.data.remote.dto.update

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskListUpdateDto(
    @field:Json(name = "id")
    val id: String? = null,
    @field:Json(name = "title")
    val title: String? = null,
    @field:Json(name = "updated")
    val updated: String? = null,
)
