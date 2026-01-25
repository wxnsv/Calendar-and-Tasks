package com.nikkap.calendar.core.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val taskDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    .withZone(ZoneId.systemDefault())

fun Long?.toReadableDate(): String {
    if (this == null) return ""
    return taskDateFormatter.format(Instant.ofEpochMilli(this))
}

fun parseIsoDate(dateStr: String?, isAllDay: Boolean = false): Long {
    if (dateStr == null) return 0L
    return try {
        val formatted = if (isAllDay && !dateStr.contains("T")) {
            "${dateStr}T00:00:00Z"
        } else {
            dateStr
        }
        Instant.parse(formatted).toEpochMilli()
    } catch (e: Exception) {
        0L
    }
}