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
        date = parseIsoDate(start.date),
        isSynced = true,
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

fun Birthday.toBirthdayEntity(): BirthdayEntity {
    return BirthdayEntity(
        id = id!!,
        name = name,
        date = date!!,
        isSynced = false,
        pendingAction = PendingActions.INSERT,
        lastModified = System.currentTimeMillis()
    )
}

fun Birthday.toBirthdayDto(): BirthdayDto {
    return BirthdayDto(
        id = id!!,
        name = name,
        start = BirthdayItemDateTime(date = date!!.toIsoDate())
    )
}


val BirthdayDto.cleanName: String?
    get() = name?.trimBirthdaySuffix()
