package com.nikkap.calendar.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TaskDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("due")
    val date: String?,
    @SerializedName("updated")
    val updated: String
)
//TODO("Correct class")
