package com.nikkap.calendar.core.utils

import android.content.Context
import com.nikkap.calendar.R
import com.nikkap.calendar.domain.model.CalendarItemType


/**
 * Removes the system suffix added by Google CalendarItem to birthday events
 * (e.g., "John Doe – Birthday" becomes "John Doe").
 * * It identifies the suffix by finding the last occurrence of a separator
 * (dash or colon) surrounded by whitespace, which is a common pattern
 * across different localized Google CalendarItem languages.
 */
fun String.trimBirthdaySuffix(): String {

    val regex = Regex("""\s+[–—:-]\s+""")
    val matches = regex.findAll(this).toList()

    return if (matches.isNotEmpty()) {
        val lastMatch = matches.last()
        this.substring(0, lastMatch.range.first).trim()
    } else {
        this
    }
}

fun CalendarItemType.toUiString(context: Context): String {
    return context.getString(
        when (this) {
            CalendarItemType.EVENT -> R.string.type_event      // "Мероприятие" или "Встреча"
            CalendarItemType.BIRTHDAY -> R.string.type_birthday // "День рождения"
            CalendarItemType.HOLIDAY -> R.string.type_holiday   // "Праздник"
        }
    )
}