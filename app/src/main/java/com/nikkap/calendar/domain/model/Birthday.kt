package com.nikkap.calendar.domain.model

data class Birthday(
    val id: String? = null,
    val name: String? = null,
    val date: Long? = null,
    val colorId: Int? = null,
) : CalendarEntry()
