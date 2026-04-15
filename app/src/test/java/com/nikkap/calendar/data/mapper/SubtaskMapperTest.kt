package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.SubtaskEntity
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.domain.model.Subtask
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


private fun testEntity(
    id: String = "id",
    title: String = "title",
    position: String = "000001",
    parentId: String = "parentId",
    isCompleted: Boolean = false,
    taskListId: String = "taskListId",
    pendingAction: PendingActions = PendingActions.NONE,
    lastModified: Long = 0L
): SubtaskEntity =
    SubtaskEntity(
        id = id,
        pendingAction = pendingAction,
        lastModified = lastModified,
        title = title,
        parentId = parentId,
        position = position,
        isCompleted = isCompleted,
        taskListId = taskListId
    )

private fun testDto(
    id: String = "test_id",
    title: String? = "test_title",
    status: String = "needsAction",
    updated: String = "2026-08-23T23:00:00Z",
    deleted: Boolean = false,
    parent: String = "parentId",
    position: String = "position"
): TaskDto =
    TaskDto(
        id = id,
        updated = updated,
        title = title,
        status = status,
        deleted = deleted,
        parent = parent,
        position = position,
        due = null
    )

private fun testDomain(
    id: String = "id",
    title: String = "title",
    position: String = "000001",
    parentId: String = "parentId",
    isCompleted: Boolean = false,
    taskListId: String = "taskListId"
): Subtask =
    Subtask(
        id = id,
        title = title,
        parentId = parentId,
        position = position,
        isCompleted = isCompleted,
        taskListId = taskListId,
    )

class SubtaskMapperTest {

    /**
     * Tests for [TaskDto.toSubtaskEntity]
     */

    @Test
    fun `dto toEntity should map all basic fields correctly`() {
        val dto = testDto(
            id = "123",
            title = "Watch a TV",
            updated = "2026-08-23T23:00:00Z",
            parent = "parentId",
            position = "00001"
        )
        val taskListId = "taskListId"

        val entity = dto.toSubtaskEntity(taskListId)

        assertEquals(dto.id, entity.id)
        assertEquals(dto.title, entity.title)
        assertEquals(dto.position, entity.position)
        assertEquals(dto.parent, entity.parentId)
        assertEquals(taskListId, entity.taskListId)
    }

    @Test
    fun `dto toEntity should map isCompleted when true correctly`() {
        val dto = testDto(
            status = "completed"
        )

        val entity = dto.toSubtaskEntity("taskListId")

        assertEquals(true, entity.isCompleted)
    }

    @Test
    fun `dto toEntity should map isCompleted when false correctly`() {
        val dto = testDto(
            status = "needsActions"
        )

        val entity = dto.toSubtaskEntity("taskListId")

        assertEquals(false, entity.isCompleted)
    }

    @Test
    fun `dto toEntity should map updated to lastModified correctly`() {
        val dto = testDto()

        val entity = dto.toSubtaskEntity("taskListId")

        assertEquals(1787526000000L, entity.lastModified)
    }

    /**
     * Tests for [SubtaskEntity.toTaskDto]
     */

    @Test
    fun `entity toDto should map all fields correctly`() {
        val entity = testEntity(
            id = "id",
            title = "Watch a TV",
            position = "000001",
            parentId = "parentId",
            isCompleted = true,
            lastModified = 0L
        )

        val dto = entity.toTaskDto()

        assertEquals(entity.id, dto.id)
        assertEquals(entity.title, dto.title)
        assertEquals("completed", dto.status)
        assertEquals(entity.parentId, dto.parent)
        assertEquals(entity.position, dto.position)
    }

    /**
     * Tests for [SubtaskEntity.markAsSynchronized]
     */

    @Test
    fun `entity markAsSynchronized should set lastUpdated to currentTime if entity's was null`() {
        val entity = testEntity()

        val actual = entity.markAsSynchronized(null, 1787526000000L)

        assertEquals(actual.lastModified, 1787526000000L)
    }


}