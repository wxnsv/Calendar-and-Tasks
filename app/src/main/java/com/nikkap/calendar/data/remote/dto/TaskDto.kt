package com.nikkap.calendar.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskDto(
    @field:Json(name = "id")
    val id: String,
    @field:Json(name = "title")
    val title: String?,
    @field:Json(name = "status")
    val status: String? = null,
    @field:Json(name = "notes")
    val notes: String? = null,
    @field:Json(name = "due")
    val deadline: String?,
    @field:Json(name = "parent")
    val parent: String? = null,
    @field:Json(name = "position")
    val position: String? = null
)
//TODO("reminders")
