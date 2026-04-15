package com.nikkap.calendar.core.utils

import com.nikkap.calendar.data.remote.dto.EventDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId

class DateUtilsTest {
    /**
     * Tests for [Long.toListUiDate]
     */

    @Test
    fun `long toListUiDate should return date if isAllDay true`() {
        val actual = 0L.toListUiDate(isAllDay = true, zoneId = ZoneId.of("UTC"))
        val expected = "01.01.1970"
        assertEquals(actual, expected)
    }

    @Test
    fun `long toListUiDate should return date and time if isAllDay false`() {
        val actual = 0L.toListUiDate(false, ZoneId.of("UTC"))
        val expected = "01.01.1970 00:00"
        assertEquals(actual, expected)
    }

    /**
     * Test for [Long.toTimeLong]
     */

    @Test
    fun `long toTimeLong should return time`() {
        val actual =
            1767225600000L.toTimeLong()
        assertEquals(0L, actual)
    }

    /**
     * Tests for [parseIsoDate]
     */

    @Test
    fun `parseIsoDate should return zero if string date null`() {
        val actual = parseIsoDate(null)
        assertEquals(0L, actual)
    }

    @Test
    fun `parseIsoDate should return long with time if isAllDay false`() {
        val actual = parseIsoDate("2026-08-23T23:00:00Z", false)
        val expected = 1787526000000L
        assertEquals(expected, actual)
    }

    @Test
    fun `parseIsoDate should return long without time if isAllDay true`() {
        val actual = parseIsoDate("2026-08-23", true)
        val expected = 1787443200000L
        assertEquals(expected, actual)
    }

    @Test
    fun `parseIsoDate should return 0L if date string is invalid`() {
        val actual = parseIsoDate("invalid-string")
        assertEquals(0L, actual)
    }

    @Test
    fun `parseIsoDate should return 0L if date format is wrong`() {
        val actual = parseIsoDate("23.08.2026 20:00")
        assertEquals(0L, actual)
    }

    /**
     * Tests for [Long.toIsoDateWithoutSeconds]
     */

    @Test
    fun `long toIsoDateWithoutSeconds should return string date in the iso format with truncated seconds`() {
        val actual = 1586521272000L.toIsoDateWithoutSeconds()
        val expected = "2020-04-10T12:21:00Z"
        assertEquals(expected, actual)
    }

    /**
     * Tests for [Long.toIsoDate]
     */

    @Test
    fun `long toIsoDate should return null if long null`() {
        val actual = null.toIsoDate()
        assertEquals(null, actual)
    }

    @Test
    fun `long toIsoDate should return string date in the ISO8601 format if long not null`() {
        val actual = 1586521272000L.toIsoDate()
        val expected = "2020-04-10T12:21:12Z"
        assertEquals(expected, actual)
    }

    /**
     * Test for [Long.toIsoDateAllDay]
     */

    @Test
    fun `long toIsoDateAllDay should return string date in the ISO8601 format without time`() {
        val actual = 1586521272000L.toIsoDateAllDay()
        val expected = "2020-04-10"
        assertEquals(expected, actual)
    }

    /**
     * Tests for [Long.toUiDate]
     */

    @Test
    fun `long toUiDate should return string date in the ISO8601 format without time`() {
        val actual = 1586521272000L.toUiTime(ZoneId.of("UTC"))
        val expected = "12:21"
        assertEquals(expected, actual)
    }

    @Test
    fun `long toUiDate should return empty string if long is null`() {
        val actual = null.toUiDate()
        val expected = ""
        assertEquals(expected, actual)
    }

    @Test
    fun `long toUiDate should return string date`() {
        val actual = 1586521272000L.toUiDate()
        val expected = "Friday, April 10, 2020"
        assertEquals(expected, actual)
    }

    /**
     * Tests for [eventDateFormatter]
     */

    @Test
    fun `eventDateFormatter should return null dateTime if isAllDay true`() {
        val pair = eventDateFormatter(
            startTimestamp = 0L,
            endTimestamp = 0L,
            isAllDay = true
        )

        val actualFirst = pair.first.dateTime
        val actualSecond = pair.second.dateTime

        val expected = null
        assertEquals(expected, actualFirst)
        assertEquals(expected, actualSecond)
    }

    @Test
    fun `eventDateFormatter should return null date if isAllDay false`() {
        val pair = eventDateFormatter(
            startTimestamp = 0L,
            endTimestamp = 0L,
            isAllDay = false
        )

        val actualFirst = pair.first.date
        val actualSecond = pair.second.date

        val expected = null
        assertEquals(expected, actualFirst)
        assertEquals(expected, actualSecond)
    }

    @Test
    fun `eventDateFormatter should return end date is a day later than start if they have an equal date timestamps`() {
        val pair = eventDateFormatter(
            startTimestamp = 0L,
            endTimestamp = 0L,
            isAllDay = true
        )

        val actualStart = pair.first.date
        val actualEnd = pair.second.date

        val expectedStart = "1970-01-01"
        val expectedEnd = "1970-01-02"
        assertEquals(expectedStart, actualStart)
        assertEquals(expectedEnd, actualEnd)
    }

    @Test
    fun `eventDateFormatter should return correct start and end dateTimes if they have an equal date timestamps`() {
        val pair = eventDateFormatter(
            startTimestamp = 0L,
            endTimestamp = 100000L,
            isAllDay = false
        )

        val actualStart = pair.first.dateTime
        val actualEnd = pair.second.dateTime

        val expectedStart = "1970-01-01T00:00:00Z"
        val expectedEnd = "1970-01-01T00:01:00Z"
        assertEquals(expectedStart, actualStart)
        assertEquals(expectedEnd, actualEnd)
    }

    @Test
    fun `eventDateFormatter should return correct pair of dates when isAllDay true`() {
        val pair = eventDateFormatter(
            startTimestamp = 0L,
            endTimestamp = 179093213L,
            isAllDay = true
        )

        val actualStart = pair.first
        val actualEnd = pair.second

        val expectedStart = EventDateTime(dateTime = null, date = "1970-01-01")
        val expectedEnd = EventDateTime(dateTime = null, date = "1970-01-03")
        assertEquals(expectedStart, actualStart)
        assertEquals(expectedEnd, actualEnd)
    }

    @Test
    fun `eventDateFormatter should return correct pair of dates when isAllDay false`() {
        val pair = eventDateFormatter(
            startTimestamp = 0L,
            endTimestamp = 0L,
            isAllDay = false
        )

        val actualStart = pair.first
        val actualEnd = pair.second

        val expectedStart = EventDateTime(dateTime = "1970-01-01T00:00:00Z", date = null)
        val expectedEnd = EventDateTime(dateTime = "1970-01-01T00:00:00Z", date = null)
        assertEquals(expectedStart, actualStart)
        assertEquals(expectedEnd, actualEnd)
    }

    /**
     * Test for [Long.toCalendar]
     */

    @Test
    fun `long toCalendar should return correct Calendar`() {
        val actual = 176576534534L.toCalendar().timeInMillis
        assertEquals(176576534534L, actual)
    }
}