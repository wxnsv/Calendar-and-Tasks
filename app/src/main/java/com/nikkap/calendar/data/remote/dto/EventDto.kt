package com.nikkap.calendar.data.remote.dto

import com.nikkap.calendar.domain.model.Event

data class EventDto(
    var id: String,
    var summary: String?,
    var startDate: String?
)

fun EventDto.toEvent(): Event {
    return Event(
        id = this.id,
        summary = this.summary,
        startDate = this.startDate
    )
}

fun Event.toEventDto(): EventDto {
    return EventDto(
        id = this.id,
        summary = this.summary,
        startDate = this.startDate
    )
}