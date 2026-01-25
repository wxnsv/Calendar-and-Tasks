package com.nikkap.calendar.domain.model

data class CalendarItem(
    val id: String,
    val summary: String?,
    val description: String?,
    /** In Holidays, Birthdays,there will be a long with no time "*/
    val startTimestamp: Long,
    val endTimestamp: Long?,
    /**  Flag for hiding the time (00:00), if date without time - true */
    val isAllDay: Boolean,
    /**  BIRTHDAY, EVENT, HOLIDAY [CalendarItemType] */
    val type: CalendarItemType,
    /**  HEX color code to match Google Calendar styling */
    val colorHex: String?,
    /**  CONFIRMED, TENTATIVE, CANCELLED [CalendarItemStatus] */
    val status: CalendarItemStatus?,
    val updated: Long
)

enum class CalendarItemType { EVENT, BIRTHDAY, HOLIDAY }
enum class CalendarItemStatus { CONFIRMED, TENTATIVE, CANCELLED }
