package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.eventIsAllDayFormatter
import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.toIsoDate
import com.nikkap.calendar.data.local.entity.EventEntity
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.remote.dto.EventDto
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.EventStatus
import java.util.Locale

fun EventDto.toEvent(): Event {
    return Event(
        id = id,
        summary = summary,
        description = description,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
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
        colorId = colorHex,
        status = status.name,
        pendingAction = PendingActions.NONE,
        lastModified = System.currentTimeMillis()
    )
}

fun EventEntity.toEvent(): Event {
    return Event(
        id = id,
        summary = summary ?: "(No title)",
        description = description,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        isAllDay = isAllDay,
        colorHex = colorId,
        status = EventStatus.valueOf(status?.uppercase() ?: ""),
    )
}

fun EventEntity.toEventDto(): EventDto {
    val (start, end) = eventIsAllDayFormatter(startTimestamp, endTimestamp, isAllDay)
    return EventDto(
        id = id,
        summary = summary,
        description = description,
        start = start,
        end = end,
        updated = lastModified.toIsoDate(),
        colorId = colorId,
        status = status
    )
}

fun EventEntity.synchronize(lastModified: Long? = null): EventEntity {
    return EventEntity(
        id = id,
        summary = summary ?: "(No title)",
        description = description,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        isAllDay = isAllDay,
        colorId = colorId,
        status = status,
        pendingAction = PendingActions.NONE,
        lastModified = lastModified ?: System.currentTimeMillis(),
    )
}

fun EventEntity.changePendingAction(pendingAction: PendingActions): EventEntity {
    return EventEntity(
        id = id,
        summary = summary ?: "(No title)",
        description = description,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        isAllDay = isAllDay,
        colorId = colorId,
        status = status,
        pendingAction = pendingAction,
        lastModified = lastModified,
    )
}

fun Event.toEventDto(): EventDto {
    val (start, end) = eventIsAllDayFormatter(startTimestamp, endTimestamp, isAllDay)
    return EventDto(
        id = id!!,
        summary = summary,
        description = description,
        start = end,
        end = start,
        updated = null,
        colorId = colorHex,
        status = status.name.lowercase(Locale.ROOT),
    )

}

fun EventDto.toEventEntity(): EventEntity {
    return EventEntity(
        id = id,
        summary = summary,
        description = description,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        isAllDay = isAllDay,
        colorId = colorId,
        status = status,
        pendingAction = PendingActions.NONE,
        lastModified = parseIsoDate(updated),
    )
}

val EventDto.startTimestamp: Long
    get() = parseIsoDate(
        (start.dateTime ?: start.date),
        start.dateTime == null
    )

val EventDto.endTimestamp: Long
    get() = parseIsoDate(
        (end.dateTime ?: end.date),
        end.dateTime == null
    )


val EventDto.isAllDay: Boolean
    get() = start.dateTime == null

val EventDto.statusType: EventStatus
    get() = try {
        EventStatus.valueOf(status?.uppercase() ?: "")
    } catch (_: Exception) {
        EventStatus.CONFIRMED
    }
