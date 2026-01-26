package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.trimBirthdaySuffix
import com.nikkap.calendar.data.local.entity.CalendarEntity
import com.nikkap.calendar.data.remote.dto.CalendarItemDto
import com.nikkap.calendar.domain.model.CalendarItem
import com.nikkap.calendar.domain.model.CalendarItemStatus
import com.nikkap.calendar.domain.model.CalendarItemType

fun CalendarItemDto.toCalendarItem(): CalendarItem {
    return CalendarItem(
        id = this.id,
        summary = this.cleanSummary,
        description = this.description,
        startTimestamp = this.startTimestamp,
        endTimestamp = this.endTimestamp,
        isAllDay = isAllDay,
        type = this.type,
        colorHex = this.colorId,
        status = this.statusType,
        updated = this.updatedLong
    )
}

fun CalendarItem.toCalendarEntity(): CalendarEntity {
    return CalendarEntity(
        id = this.id,
        summary = this.summary,
        description = this.description,
        startTimestamp = this.startTimestamp,
        endTimestamp = this.endTimestamp,
        isAllDay = this.isAllDay,
        type = this.type.name,
        colorHex = this.colorHex,
        status = this.status.name,
        updated = this.updated
    )
}

fun CalendarEntity.toCalendarItem(): CalendarItem {
    return CalendarItem(
        id = this.id,
        summary = this.summary ?: "(No title)",
        description = this.description,
        startTimestamp = this.startTimestamp,
        endTimestamp = this.endTimestamp,
        isAllDay = this.isAllDay,
        type = CalendarItemType.valueOf(this.type.uppercase()),
        colorHex = this.colorHex,
        status = CalendarItemStatus.valueOf(this.status.uppercase()),
        updated = this.updated,
    )
}

//fun CalendarItem.toEventDto(): CalendarItemDto {
//    return CalendarItemDto(
//        id = this.id,
//        summary = this.summary,
//        startDate = this.startDate
//    )
//}
fun CalendarItemDto.toCalendarEntity(): CalendarEntity {
    return CalendarEntity(
        id = this.id,
        summary = this.cleanSummary,
        description = this.description,
        startTimestamp = this.startTimestamp,
        endTimestamp = this.endTimestamp,
        isAllDay = this.isAllDay,
        type = this.eventType,
        colorHex = this.colorId,
        status = this.status,
        updated = this.updatedLong
    )
}

val CalendarItemDto.updatedLong: Long
    get() = parseIsoDate(updated)
val CalendarItemDto.startTimestamp: Long
    get() = parseIsoDate(
        (start?.dateTime ?: start?.date).toString(),
        start?.dateTime == null
    )

val CalendarItemDto.endTimestamp: Long?
    get() = end?.let {
        parseIsoDate((it.dateTime ?: it.date).toString(), it.dateTime == null)
    }

val CalendarItemDto.isAllDay: Boolean
    get() = start?.dateTime == null

val CalendarItemDto.type: CalendarItemType
    get() = try {
        CalendarItemType.valueOf(this.eventType.uppercase())
    } catch (e: Exception) {
        CalendarItemType.DEFAULT // Дефолтное значение
    }

val CalendarItemDto.statusType: CalendarItemStatus
    get() = try {
        CalendarItemStatus.valueOf(this.status.uppercase())
    } catch (e: Exception) {
        CalendarItemStatus.CONFIRMED
    }

val CalendarItemDto.cleanSummary: String?
    get() = if (this.type == CalendarItemType.BIRTHDAY) {
        summary?.trimBirthdaySuffix()
    } else {
        summary
    }