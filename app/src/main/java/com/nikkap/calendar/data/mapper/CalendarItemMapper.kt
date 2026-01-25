package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.trimBirthdaySuffix
import com.nikkap.calendar.data.remote.dto.CalendarItemDto
import com.nikkap.calendar.domain.model.CalendarItem
import com.nikkap.calendar.domain.model.CalendarItemStatus
import com.nikkap.calendar.domain.model.CalendarItemType

fun CalendarItemDto.toCalendarItem(): CalendarItem {
    val isAllDay = this.start?.dateTime == null
    val startStr = this.start?.dateTime ?: this.start?.date
    val endStr = this.end?.dateTime ?: this.end?.date
    val startLong = parseIsoDate(startStr.toString(), isAllDay)
    val endLong = parseIsoDate(endStr.toString(), isAllDay)
    val updated = parseIsoDate(this.updated)
    val summary = this.summary ?: "(No title)"
    val type = when {
        this.eventType == "birthday" -> CalendarItemType.BIRTHDAY
        this.eventType == "holiday" -> CalendarItemType.HOLIDAY
        else -> CalendarItemType.EVENT
    }
    val status = when {
        this.status == "cancelled" -> CalendarItemStatus.CANCELLED
        this.status == "confirmed" -> CalendarItemStatus.CONFIRMED
        this.status == "tentative" -> CalendarItemStatus.TENTATIVE
        else -> null
    }
    val cleanSummary = if (type == CalendarItemType.BIRTHDAY) {
        summary.trimBirthdaySuffix()
    } else {
        summary
    }
    return CalendarItem(
        id = this.id,
        summary = cleanSummary,
        description = this.description,
        startTimestamp = startLong,
        endTimestamp = endLong,
        isAllDay = isAllDay,
        type = type,
        colorHex = this.colorId,
        status = status,
        updated = updated
    )
}

//fun CalendarItem.toEventDto(): CalendarItemDto {
//    return CalendarItemDto(
//        id = this.id,
//        summary = this.summary,
//        startDate = this.startDate
//    )
//}