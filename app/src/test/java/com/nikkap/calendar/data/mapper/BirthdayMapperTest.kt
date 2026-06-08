package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.data.local.entity.BirthdayEntity
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.remote.dto.BirthdayDateTime
import com.nikkap.calendar.data.remote.dto.BirthdayDto
import com.nikkap.calendar.domain.model.Birthday
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private fun testEntity(
    id: String = "default_id",
    name: String = "Default Name",
    date: Long = 0L,
    pendingAction: PendingActions = PendingActions.NONE,
    lastModified: Long = 0L
) = BirthdayEntity(
    id = id,
    name = name,
    date = date,
    pendingAction = pendingAction,
    lastModified = lastModified,
    colorId = 2
)

private fun testDto(
    id: String = "default_id",
    summary: String = "Default Name",
    date: String = "1970-01-01",
    updated: String = "2020-04-10T12:21:00Z",
    colorId: Int = 2
): BirthdayDto =
    BirthdayDto(
        id = id,
        summary = summary,
        start = BirthdayDateTime(date),
        updated = updated,
        colorId = colorId.toString()
    )

fun testBirthday(
    id: String? = "default_id",
    name: String = "Default Name",
    date: Long? = 0L,
    color: Int = 2
) = Birthday(
    id = id,
    name = name,
    date = date,
    colorId = color
)


class BirthdayMapperTest {

    /**
     * Tests for [BirthdayDto.toBirthdayEntity]
     */

    @Test
    fun `dto toEntity should map all basic fields correctly`() {
        val dto = testDto(id = "1", summary = "John")
        val entity = dto.toBirthdayEntity()

        assertEquals("1", entity.id)
        assertEquals("John", entity.name)
        assertEquals(PendingActions.NONE, entity.pendingAction)
    }

    @Test
    fun `dto toEntity should call parseIsoDate with correct allDay flag`() {
        val dto = testDto(date = "2026-01-01")
        val entity = dto.toBirthdayEntity()

        assertEquals(1767225600000L, entity.date)
    }

    /**
     * Tests for [BirthdayEntity.toBirthdayDto]
     */

    @Test
    fun `entity toDto should map all fields correctly`() {
        val entity = testEntity(
            id = "123",
            name = "John",
            lastModified = 1787526000000L,
            date = 1787526000000L
        )

        val dto = entity.toBirthdayDto()

        assertEquals("123", dto.id)
        assertEquals("John", dto.summary)
        assertEquals("2026-08-23", dto.start.date)
    }

    /**
     * Tests for [BirthdayEntity.markAsSynchronized]
     */

    @Test
    fun `entity markAsSynchronized should set lastUpdated to currentTime if entity's was null`() {
        val entity = testEntity()

        val actual = entity.markAsSynchronized(null, 1787526000000L)

        assertEquals(actual.lastModified, 1787526000000L)
    }

    /**
     * Tests for [com.nikkap.calendar.domain.model.Birthday.toBirthdayEntity]
     */

    @Test
    fun `domain toEntity should map all fields correctly and use currentTime`() {
        val domain = testBirthday(
            id = "123",
            name = "John",
            date = 0L,
            color = 1
        )

        val entity = domain.toBirthdayEntity(1767225600000L)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.date, entity.date)
        assertEquals(PendingActions.NONE, entity.pendingAction)
        assertEquals(1767225600000L, entity.lastModified)
    }

    /**
     * Tests for [com.nikkap.calendar.domain.model.Birthday.toBirthdayDto]
     */

    @Test
    fun `domain toDto should map all fields correctly`() {
        val domain = testBirthday(
            id = "123",
            name = "John",
            date = 1787526000000L
        )

        val dto = domain.toBirthdayDto()

        assertEquals(domain.id, dto.id)
        assertEquals(domain.name, dto.summary)
        assertEquals("2026-08-23T23:00:00Z", dto.start.date)
    }
}