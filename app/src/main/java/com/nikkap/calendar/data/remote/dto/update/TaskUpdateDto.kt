package com.nikkap.calendar.data.remote.dto.update

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskUpdateDto(
    @field:Json(name = "id")
    val id: String? = null,
    @field:Json(name = "title")
    val title: String? = null,
    @field:Json(name = "status")
    val status: String? = null,
    @field:Json(name = "notes")
    val notes: String? = null,
    @field:Json(name = "due")
    val due: String? = null,
    @field:Json(name = "parent")
    val parent: String? = null,
    @field:Json(name = "position")
    val position: String? = null,
    @field:Json(name = "updated")
    val updated: String? = null,
)
