package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.toIsoDateAllDay
import com.nikkap.calendar.core.utils.toIsoDateWithoutSeconds
import com.nikkap.calendar.core.utils.trimBirthdaySuffix
import com.nikkap.calendar.data.local.entity.BirthdayEntity
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.remote.dto.BirthdayDateTime
import com.nikkap.calendar.data.remote.dto.BirthdayDto
import com.nikkap.calendar.data.remote.dto.update.BirthdayUpdateDto
import com.nikkap.calendar.domain.model.Birthday

fun BirthdayDto.toBirthdayEntity(): BirthdayEntity {
    return BirthdayEntity(
        id = id,
        name = summary?.trimBirthdaySuffix(),
        date = parseIsoDate(start.date, true),
        pendingAction = PendingActions.NONE,
        lastModified = parseIsoDate(updated),
        colorId = colorId?.toInt() ?: 2
    )
}

fun BirthdayEntity.toBirthday(): Birthday {
    return Birthday(
        id = id,
        name = name,
        date = date,
        colorId = colorId
    )
}

fun Birthday.toBirthdayUpdateDto(): BirthdayUpdateDto {
    return BirthdayUpdateDto(
        id = id!!,
        summary = if (name.isNullOrBlank()) null else name,
        start = if (date == 0L || date == null) null else BirthdayDateTime(date.toIsoDateAllDay()),
        colorId = colorId?.toString()
    )
}

fun BirthdayEntity.toBirthdayDto(): BirthdayDto {
    return BirthdayDto(
        id = id,
        summary = name,
        start = BirthdayDateTime(date.toIsoDateAllDay()),
        colorId = colorId.toString()
    )
}

fun BirthdayEntity.changePendingAction(pendingAction: PendingActions): BirthdayEntity {
    return this.copy(pendingAction = pendingAction)

}

fun BirthdayEntity.markAsSynchronized(
    lastModified: Long? = null,
    currentTime: Long = System.currentTimeMillis()
): BirthdayEntity {
    return this.copy(
        pendingAction = PendingActions.NONE,
        lastModified = lastModified ?: currentTime,
    )
}

fun Birthday.toBirthdayEntity(currentTime: Long = System.currentTimeMillis()): BirthdayEntity {
    return BirthdayEntity(
        id = id!!,
        name = name,
        date = date!!,
        pendingAction = PendingActions.NONE,
        lastModified = currentTime,
        colorId = colorId ?: 2
    )
}

fun Birthday.toBirthdayDto(): BirthdayDto {
    return BirthdayDto(
        id = id!!,
        summary = name,
        start = BirthdayDateTime(date = date!!.toIsoDateWithoutSeconds()),
        colorId = colorId.toString()
    )
}