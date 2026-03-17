package com.nikkap.calendar.domain.model

import com.nikkap.calendar.core.utils.CalendarColors

data class Birthday(
    val id: String? = null,
    val name: String? = null,
    val date: Long? = 0L,
    val colorHex: String = CalendarColors.getBirthdayColor(null).id,
) : CalendarEntry()
