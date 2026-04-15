package com.nikkap.calendar.core.utils

import com.nikkap.calendar.data.remote.dto.EventDateTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

/**
 * Functions labeled Ui are designed
 * to display on the user's screen
 * and can use the phone's system Time Zone.
 */
fun Long.toListUiDate(isAllDay: Boolean = true, zoneId: ZoneId = ZoneId.systemDefault()): String {
    val allDayDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        .withZone(zoneId)

    val defaultDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(zoneId)

    return if (isAllDay) allDayDateFormatter.format(Instant.ofEpochMilli(this))
    else defaultDateFormatter.format(Instant.ofEpochMilli(this))
}

fun Long.toTimeLong(): Long {
    val millisInDay = 24 * 60 * 60 * 1000L

    val instant = Instant.ofEpochMilli(this)
    val offsetMillis = ZoneId.of("UTC").rules.getOffset(instant).totalSeconds * 1000L

    return (this + offsetMillis) % millisInDay
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

fun Long.toIsoDateWithoutSeconds(): String {
    val instant = Instant.ofEpochMilli(this).truncatedTo(ChronoUnit.MINUTES)

    return DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")).format(instant)
}

fun Long.toIsoDateWithoutTime(): String {
    val instant = Instant.ofEpochMilli(this).truncatedTo(ChronoUnit.DAYS)

    return DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")).format(instant)
}

fun Long?.toIsoDate(): String? {
    return if (this != null) Instant.ofEpochMilli(this)
        .atOffset(ZoneOffset.UTC)
        .format(DateTimeFormatter.ISO_INSTANT)
    else null
}

fun Long.toIsoDateAllDay(): String {
    val instant = Instant.ofEpochMilli(this)

    return instant.atOffset(ZoneOffset.UTC)
        .format(DateTimeFormatter.ISO_INSTANT).take(10)
}


fun Long.toUiTime(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    return Instant.ofEpochMilli(this)
        .atZone(zoneId)
        .format(formatter)
}

fun Long?.toUiDate(): String {
    if (this == null) return ""
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.ENGLISH)
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

private fun eventDateFormatter(isAllDay: Boolean, timeStamp: Long): EventDateTime {
    return if (isAllDay) EventDateTime(
        dateTime = null,
        date = timeStamp.toIsoDateAllDay()
    ) else EventDateTime(
        dateTime = timeStamp.toIsoDateWithoutSeconds(),
        date = null
    )
}

fun eventDateFormatter(
    startTimestamp: Long,
    endTimestamp: Long,
    isAllDay: Boolean
): Pair<EventDateTime, EventDateTime> {
    if (!isAllDay) return Pair(
        eventDateFormatter(false, startTimestamp),
        eventDateFormatter(false, endTimestamp)
    )
    val startDate =
        Instant.ofEpochMilli(startTimestamp).atZone(ZoneId.of("UTC")).toLocalDate()
    val endDate = Instant.ofEpochMilli(endTimestamp).atZone(ZoneId.of("UTC")).toLocalDate()

    val returnEndDate: LocalDate

    if (startDate == endDate) {
        returnEndDate = endDate.plusDays(1)
    } else returnEndDate = endDate

    return Pair(
        eventDateFormatter(
            true,
            startDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        ),
        eventDateFormatter(
            true,
            returnEndDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
    )
}

fun Long.toCalendar(): Calendar {
    return Calendar.getInstance().apply {
        timeInMillis = this@toCalendar
    }
}