package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.toIsoDate
import com.nikkap.calendar.core.utils.trimBirthdaySuffix
import com.nikkap.calendar.data.local.entity.BirthdayEntity
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.remote.dto.BirthdayDto
import com.nikkap.calendar.data.remote.dto.BirthdayItemDateTime
import com.nikkap.calendar.domain.model.Birthday

fun BirthdayDto.toBirthdayEntity(): BirthdayEntity {
    return BirthdayEntity(
        id = id,
        name = cleanName,
        date = parseIsoDate(start.date, true),
        pendingAction = PendingActions.NONE,
        lastModified = parseIsoDate(updated)
    )
}

fun BirthdayEntity.toBirthday(): Birthday {
    return Birthday(
        id = id,
        name = name,
        date = date
    )
}

fun BirthdayEntity.toBirthdayDto(): BirthdayDto {
    return BirthdayDto(
        id = id,
        summary = name,
        start = BirthdayItemDateTime(date.toIsoDate()),
        updated = lastModified.toIsoDate()
    )
}

fun BirthdayEntity.changePendingAction(pendingAction: PendingActions): BirthdayEntity {
    return BirthdayEntity(
        id = id,
        name = name,
        date = date,
        pendingAction = pendingAction,
        lastModified = lastModified
    )
}

fun BirthdayEntity.synchronize(lastModified: Long? = null): BirthdayEntity {
    return BirthdayEntity(
        id = id,
        name = name,
        date = date,
        pendingAction = PendingActions.NONE,
        lastModified = lastModified ?: System.currentTimeMillis(),
    )
}

fun Birthday.toBirthdayEntity(): BirthdayEntity {
    return BirthdayEntity(
        id = id!!,
        name = name,
        date = date!!,
        pendingAction = PendingActions.NONE,
        lastModified = System.currentTimeMillis()
    )
}

fun Birthday.toBirthdayDto(): BirthdayDto {
    return BirthdayDto(
        id = id!!,
        summary = name,
        start = BirthdayItemDateTime(date = date!!.toIsoDate())
    )
}


val BirthdayDto.cleanName: String?
    get() = summary?.trimBirthdaySuffix()
