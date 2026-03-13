package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.calendarDateFormatter
import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.data.local.entity.EventEntity
import com.nikkap.calendar.data.remote.dto.EventDto
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.EventStatus
import java.util.Locale

fun EventDto.toEvent(): Event {
    return Event(
        id = id!!,
        summary = summary,
        description = description,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp!!,
        isAllDay = isAllDay,
        colorHex = colorId,
        status = statusType,
    )
}

fun Event.toEventEntity(): EventEntity {
    return EventEntity(
        id = id!!,
        summary = summary,
        description = description,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        isAllDay = isAllDay,
        colorHex = colorHex,
        status = status.name,
    )
}

fun EventEntity.toEvent(): Event {
    return Event(
        id = id,
        summary = summary ?: "(No title)",
        description = description,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp!!,
        isAllDay = isAllDay,
        colorHex = colorHex,
        status = EventStatus.valueOf(status?.uppercase() ?: ""),
    )
}

fun Event.toEventDto(isEdit: Boolean = false): EventDto {
    return EventDto(
        id = if (isEdit) null else id,
        summary = summary,
        description = description,
        start = calendarDateFormatter(isAllDay, startTimestamp),
        end = calendarDateFormatter(isAllDay, endTimestamp),
        updated = null,
        colorId = colorHex,
        status = status.name.lowercase(Locale.ROOT),
    )
}

fun EventDto.toEventEntity(): EventEntity {
    return EventEntity(
        id = id!!,
        summary = summary,
        description = description,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        isAllDay = isAllDay,
        colorHex = colorId,
        status = status,
    )
}

val EventDto.startTimestamp: Long
    get() = parseIsoDate(
        (start.dateTime ?: start.date).toString(),
        start.dateTime == null
    )

val EventDto.endTimestamp: Long?
    get() = end?.let {
        parseIsoDate((it.dateTime ?: it.date).toString(), it.dateTime == null)
    }

val EventDto.isAllDay: Boolean
    get() = start.dateTime == null

val EventDto.statusType: EventStatus
    get() = try {
        EventStatus.valueOf(status?.uppercase() ?: "")
    } catch (_: Exception) {
        EventStatus.CONFIRMED
    }
