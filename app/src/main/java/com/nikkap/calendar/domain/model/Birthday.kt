package com.nikkap.calendar.domain.model

import com.nikkap.calendar.core.utils.CalendarColors
import com.nikkap.calendar.data.remote.dto.BirthdayItemDateTime

data class Birthday(
    val id: String = "",
    val name: String? = null,
    val date: BirthdayItemDateTime = BirthdayItemDateTime(
        date = null
    ),
    val colorHex: String = CalendarColors.getBirthdayColor(null).id,
) : CalendarEntry()
