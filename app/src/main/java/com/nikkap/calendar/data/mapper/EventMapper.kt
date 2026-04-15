package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.eventDateFormatter
import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.data.local.entity.EventEntity
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.remote.dto.EventDto
import com.nikkap.calendar.domain.model.Event
import com.nikkap.calendar.domain.model.EventStatus
import java.util.Locale

fun Event.toEventEntity(lastModified: Long = System.currentTimeMillis()): EventEntity {
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
        lastModified = lastModified
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
    val (start, end) =
        eventDateFormatter(startTimestamp, endTimestamp, isAllDay)
    return EventDto(
        id = id,
        summary = summary,
        description = description,
        start = start,
        end = end,
        updated = "",
        colorId = colorId,
        status = status
    )
}

fun EventEntity.markAsSynchronized(
    lastModified: Long? = null,
    currentTime: Long = System.currentTimeMillis()
): EventEntity {
    return this.copy(
        pendingAction = PendingActions.NONE,
        lastModified = lastModified ?: currentTime
    )
}

fun EventEntity.changePendingAction(pendingAction: PendingActions): EventEntity {
    return this.copy(
        pendingAction = pendingAction,
    )
}

fun Event.toEventDto(): EventDto {
    val (start, end) = eventDateFormatter(startTimestamp, endTimestamp, isAllDay)
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
