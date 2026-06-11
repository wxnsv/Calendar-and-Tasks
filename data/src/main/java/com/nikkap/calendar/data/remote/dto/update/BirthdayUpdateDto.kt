package com.nikkap.calendar.data.remote.dto.update

import com.nikkap.calendar.data.remote.dto.BirthdayDateTime

data class BirthdayUpdateDto(
    val id: String? = null,
    val summary: String? = null,
    val start: BirthdayDateTime? = null,
    val end: BirthdayDateTime? = null,
    val colorId: String? = null,
    val transparency: String = "transparent",
    val updated: String? = null
)
