package com.nikkap.calendar.data.remote.dto.update

import com.nikkap.calendar.data.remote.dto.BirthdayDateTime

data class BirthdayUpdateDto(
    val id: String? = null,
    val summary: String? = null,
    val start: BirthdayDateTime? = null,
    val colorId: String? = null,
)
