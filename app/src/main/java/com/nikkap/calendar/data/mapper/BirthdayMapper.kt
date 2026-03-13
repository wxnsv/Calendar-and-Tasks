package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.trimBirthdaySuffix
import com.nikkap.calendar.data.local.entity.BirthdayEntity
import com.nikkap.calendar.data.remote.dto.BirthdayDto
import com.nikkap.calendar.data.remote.dto.BirthdayItemDateTime
import com.nikkap.calendar.domain.model.Birthday

fun BirthdayDto.toBirthdayEntity(): BirthdayEntity {
    return BirthdayEntity(
        id = id,
        name = cleanName,
        date = parseIsoDate(date.date)
    )
}

fun BirthdayEntity.toBirthday(): Birthday {
    return Birthday(
        id = id,
        name = name,
        date = BirthdayItemDateTime(date = date.toString())
    )
}

fun Birthday.toBirthdayEntity(): BirthdayEntity {
    return BirthdayEntity(
        id = id,
        name = name,
        date = parseIsoDate(date.date)
    )
}

fun Birthday.toBirthdayDto(): BirthdayDto {
    return BirthdayDto(
        id = id,
        name = id,
        date = date
    )
}


val BirthdayDto.cleanName: String?
    get() = name?.trimBirthdaySuffix()
