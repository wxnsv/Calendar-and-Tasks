package com.nikkap.calendar.core.utils

import com.nikkap.calendar.data.remote.dto.CalendarItemDateTime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

private val AllDayDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    .withZone(ZoneId.systemDefault())
private val DefaultDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    .withZone(ZoneId.systemDefault())

fun Long?.toListDate(isAllDay: Boolean = true): String {
    if (this == null) return ""
    return if (isAllDay) AllDayDateFormatter.format(Instant.ofEpochMilli(this))
    else DefaultDateFormatter.format(Instant.ofEpochMilli(this))
}

fun Long.toTimeLong(): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this

    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    return Calendar.getInstance().apply {
        set(Calendar.YEAR, 1970)
        set(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }.timeInMillis
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
    } catch (_: Exception) {
        0L
    }
}

fun Long.toIsoDate(): String {
    val instant = Instant.ofEpochMilli(this).truncatedTo(ChronoUnit.MINUTES)

    return DateTimeFormatter.ISO_INSTANT.format(instant)
}

fun Long.toIsoDateAllDay(): String {
    val instant = Instant.ofEpochMilli(this)

    return DateTimeFormatter.ISO_INSTANT.format(instant).take(10)
}


fun Long.toTime(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

fun Long.toDate(): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.ENGLISH)
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

fun calendarDateFormatter(isAllDay: Boolean, timeStamp: Long): CalendarItemDateTime {
    return if (isAllDay) CalendarItemDateTime(
        dateTime = null,
        date = timeStamp.toIsoDateAllDay()
    ) else CalendarItemDateTime(dateTime = timeStamp.toIsoDate(), date = null)
}