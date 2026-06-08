package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.toIsoDate
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.TaskEntity
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.domain.model.Task
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource


private fun testEntity(
    id: String = "test_id",
    title: String? = "test_title",
    notes: String? = "test_notes",
    isCompleted: Boolean = false,
    deadline: Long? = 0L,
    lastModified: Long = 0L,
    taskListId: String = "taskList_Id",
): TaskEntity =
    TaskEntity(
        id = id,
        pendingAction = PendingActions.NONE,
        lastModified = lastModified,
        title = title,
        notes = notes,
        isCompleted = isCompleted,
        deadline = deadline,
        taskListId = taskListId
    )

private fun testDto(
    id: String = "test_id",
    title: String? = "test_title",
    notes: String? = "test_notes",
    status: String = "needsAction",
    due: String? = "1970-01-01",
    updated: String = "2026-08-23T23:00:00Z",
    deleted: Boolean = false
): TaskDto =
    TaskDto(
        id = id,
        updated = updated,
        title = title,
        notes = notes,
        status = status,
        due = due,
        deleted = deleted
    )

class TaskMapperTest {

    /**
     * Tests for [com.nikkap.calendar.domain.model.Task.toTaskDto]
     */

    @Test
    fun `domain toDto should map all basic fields correctly`() {
        val domain = Task(
            id = "123",
            title = "Do work",
            notes = "Watch a TV",
            deadline = 1767225600000L,
        )

        val dto = domain.toTaskDto()

        assertEquals(domain.id, dto.id)
        assertEquals(domain.title, dto.title)
        assertEquals(domain.notes, dto.notes)
        assertEquals(domain.deadline!!.toIsoDate(), dto.due)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `domain toDto should map isComplete correctly`(isCompleted: Boolean) {
        val domain = Task(
            id = "id",
            isCompleted = isCompleted
        )
        val dtoIsCompleted = if (isCompleted) "completed" else "needsAction"

        val dto = domain.toTaskDto()

        assertEquals(dtoIsCompleted, dto.status)
    }

    /**
     * Tests for [TaskEntity.toTask]
     */

    @Test
    fun `entity to domain should map title to text if entity's is null`() {
        val entity = testEntity(title = null)

        val domain = entity.toTask()

        assertEquals("(No title)", domain.title)
    }

    /**
     * Tests for [com.nikkap.calendar.domain.model.Task.toTaskEntity]
     */

    @Test
    fun `domain toEntity should map all fields correctly and use currentTime`() {
        val domain = Task(
            id = "123",
            title = "Do work",
            notes = "Watch a TV",
            deadline = 1767225600000L,
            isCompleted = false,
            taskListId = "taskListId",
        )

        val entity = domain.toTaskEntity(1767225600000L)

        assertEquals(domain.id, entity.id)
        assertEquals(domain.title, entity.title)
        assertEquals(domain.notes, entity.notes)
        assertEquals(domain.deadline, entity.deadline)
        assertEquals(domain.isCompleted, entity.isCompleted)
        assertEquals(PendingActions.NONE, entity.pendingAction)
        assertEquals(1767225600000L, entity.deadline)
        assertEquals(1767225600000L, entity.lastModified)
    }

    /**
     * Tests for [TaskDto.toTaskEntity]
     */

    @Test
    fun `dto toEntity should map basic field correctly`() {
        val dto = testDto(
            id = "123",
            title = "Do work",
            notes = "Watch a TV",
            status = "needsAction",
            due = "1970-01-02",
        )
        val taskListId = "listId"

        val entity = dto.toTaskEntity(taskListId)

        assertEquals(dto.id, entity.id)
        assertEquals(dto.title, entity.title)
        assertEquals(dto.notes, entity.notes)
        assertEquals(PendingActions.NONE, PendingActions.NONE)
        assertEquals(taskListId, entity.taskListId)
    }

    @ParameterizedTest
    @ValueSource(strings = ["needsAction", "completed"])
    fun `dto toEntity should map isComplete correctly`(isCompleted: String) {
        val dto = testDto(status = isCompleted)
        val taskListId = "listId"

        val entity = dto.toTaskEntity(taskListId)
        val dtoIsCompleted = isCompleted == "completed"

        assertEquals(dtoIsCompleted, entity.isCompleted)
    }

    @Test
    fun `dto toEntity should map updated to lastModified correctly`() {
        val dto = testDto(
            updated = "2026-08-23T23:00:00Z"
        )
        val taskListId = "listId"

        val entity = dto.toTaskEntity(taskListId)

        assertEquals(1787526000000L, entity.lastModified)
    }

    @Test
    fun `dto toEntity should map due to deadline correctly`() {
        val dto = testDto(
            due = "2026-08-23T23:00:00Z"
        )
        val taskListId = "listId"

        val entity = dto.toTaskEntity(taskListId)

        assertEquals(1787526000000, entity.deadline)
    }

    /**
     * Tests for [TaskEntity.toTaskDto]
     */

    @Test
    fun `entity toDto should map basic field correctly`() {
        val entity = testEntity(
            id = "123",
            title = "Do work",
            notes = "Watch a TV",
            isCompleted = false,
        )

        val dto = entity.toTaskDto()

        assertEquals(entity.id, dto.id)
        assertEquals(entity.title, dto.title)
        assertEquals(entity.notes, dto.notes)
        assertEquals(null, dto.parent)
        assertEquals(null, dto.position)
    }

    @Test
    fun `entity toDto should map isComplete when false correctly`() {
        val entity = testEntity(
            isCompleted = false,
        )

        val dto = entity.toTaskDto()

        assertEquals(false, dto.isCompleted)
    }

    @Test
    fun `entity toDto should map isComplete when true correctly`() {
        val entity = testEntity(
            isCompleted = true,
        )

        val dto = entity.toTaskDto()

        assertEquals(true, dto.isCompleted)
    }

    @Test
    fun `entity toDto should map deadline field correctly`() {
        val entity = testEntity(deadline = 1787526000000)

        val dto = entity.toTaskDto()

        assertEquals("2026-08-23T00:00:00Z", dto.due)
    }

    /**
     * Tests for [TaskEntity.markAsSynchronized]
     */

    @Test
    fun `entity markAsSynchronized should set lastUpdated to currentTime if entity's was null`() {
        val entity = testEntity()

        val actual = entity.markAsSynchronized(null, 1787526000000L)

        assertEquals(actual.lastModified, 1787526000000L)
    }
}