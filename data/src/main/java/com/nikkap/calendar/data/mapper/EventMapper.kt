package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.eventDateFormatter
import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.data.local.entity.EventEntity
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.remote.dto.EventDateTime
import com.nikkap.calendar.data.remote.dto.EventDto
import com.nikkap.calendar.data.remote.dto.update.EventUpdateDto
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
        colorId = colorId ?: 7,
        status = status?.name,
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
        colorId = colorId,
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
        start = EventDateTime(start.first, start.second),
        end = EventDateTime(end.first, end.second),
        updated = null,
        colorId = colorId.toString(),
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
        start = EventDateTime(start.first, start.second),
        end = EventDateTime(end.first, end.second),
        updated = null,
        colorId = colorId.toString(),
        status = status?.name?.lowercase(Locale.ROOT),
    )
}

fun Event.toEventUpdateDto(): EventUpdateDto {
    val (start, end) = eventDateFormatter(startTimestamp, endTimestamp, isAllDay)
    return EventUpdateDto(
        id = id,
        summary = if (summary.isNullOrBlank()) null else summary,
        description = if (description.isNullOrBlank()) null else description,
        start = if (startTimestamp == 0L) null else EventDateTime(start.first, start.second),
        end = if (endTimestamp == 0L) null else EventDateTime(end.first, end.second),
        colorId = colorId?.toString(),
        status = status?.name?.lowercase(Locale.ROOT),
    )
}

fun EventEntity.toEventUpdateDto(): EventUpdateDto {
    val (start, end) = eventDateFormatter(startTimestamp, endTimestamp, isAllDay)
    return EventUpdateDto(
        id = id,
        summary = if (summary.isNullOrBlank()) null else summary,
        description = if (description.isNullOrBlank()) null else description,
        start = if (startTimestamp == 0L) null else EventDateTime(start.first, start.second),
        end = if (endTimestamp == 0L) null else EventDateTime(end.first, end.second),
        colorId = 3.toString(),
        status = null,
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
        colorId = colorId?.toInt() ?: 7,
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
