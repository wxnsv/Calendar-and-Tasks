package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.toIsoDate
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.SubtaskEntity
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.data.remote.dto.update.TaskUpdateDto
import com.nikkap.calendar.domain.model.Subtask

fun TaskDto.toSubtaskEntity(taskListId: String): SubtaskEntity {
    return SubtaskEntity(
        id = id,
        title = title,
        parentId = parent!!,
        position = position!!,
        isCompleted = isCompleted,
        pendingAction = PendingActions.NONE,
        lastModified = parseIsoDate(updated, true),
        taskListId = taskListId,
        deadline = parseIsoDate(due)
    )
}

fun TaskUpdateDto.toSubtaskEntity(taskListId: String): SubtaskEntity {
    return SubtaskEntity(
        id = id!!,
        title = title,
        parentId = parent!!,
        position = position!!,
        isCompleted = status == "completed",
        pendingAction = PendingActions.NONE,
        lastModified = parseIsoDate(updated, true),
        taskListId = taskListId,
        deadline = parseIsoDate(due)
    )
}

fun SubtaskEntity.toSubtask(): Subtask {
    return Subtask(
        id = id,
        title = title,
        parentId = parentId,
        position = position,
        isCompleted = isCompleted,
        taskListId = taskListId,
        deadline = deadline
    )
}

fun Subtask.toSubtaskEntity(lastModified: Long = System.currentTimeMillis()): SubtaskEntity {
    return SubtaskEntity(
        id = id,
        title = title,
        parentId = parentId,
        position = position,
        isCompleted = isCompleted,
        pendingAction = PendingActions.NONE,
        lastModified = lastModified,
        taskListId = taskListId,
        deadline = deadline
    )
}

fun SubtaskEntity.toTaskDto(): TaskDto {
    return TaskDto(
        id = id,
        title = title,
        status = isCompletedString,
        parent = parentId,
        position = position,
        due = null,
    )
}

fun SubtaskEntity.markAsSynchronized(
    lastModified: Long? = null,
    currentTime: Long = System.currentTimeMillis()
): SubtaskEntity {
    return this.copy(
        pendingAction = PendingActions.NONE,
        lastModified = lastModified ?: currentTime
    )
}

fun SubtaskEntity.changePendingAction(pendingAction: PendingActions): SubtaskEntity {
    return this.copy(
        pendingAction = pendingAction,
    )
}

fun Subtask.toTaskUpdateDto(): TaskUpdateDto {
    return TaskUpdateDto(
        id = id,
        title = if (title.isNullOrBlank()) null else title,
        status = if (isCompleted) "completed" else "needsAction",
        due = if (deadline != null && deadline != 0L) deadline.toIsoDate() else null,
        parent = parentId,
        position = position.ifBlank { null },
    )
}

fun SubtaskEntity.toTaskUpdateDto(): TaskUpdateDto {
    return TaskUpdateDto(
        id = id,
        title = if (title.isNullOrBlank()) null else title,
        status = if (isCompleted) "completed" else "needsAction",
        due = if (deadline != null && deadline != 0L) deadline.toIsoDate() else null,
        parent = parentId,
        position = position.ifBlank { null },
    )
}

val SubtaskEntity.isCompletedString: String
    get() = if (isCompleted) "completed" else "needsAction"