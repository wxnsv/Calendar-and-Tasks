package com.nikkap.calendar.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CalendarItemDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("summary")
    val summary: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("start")
    val start: CalendarItemDateTime?,
    @SerializedName("end")
    val end: CalendarItemDateTime?,
    @SerializedName("updated")
    val updated: String?,
    @SerializedName("colorId")
    val colorId: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("eventType")
    val eventType: String,
//    @SerializedName("reminders")
//    val reminders: RemindersDto?,
)

data class CalendarItemDateTime(
    @SerializedName("dateTime")
    val dateTime: String?,
    @SerializedName("date")
    val date: String?
)

