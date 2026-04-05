package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.toIsoDate
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.SubtaskEntity
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.domain.model.Subtask

fun TaskDto.toSubtaskEntity(taskListId: String): SubtaskEntity {
    return SubtaskEntity(
        id = id,
        title = title,
        parentId = parent!!,
        position = position.toString(),
        isCompleted = isCompleted,
        pendingAction = PendingActions.NONE,
        lastModified = parseIsoDate(updated),
        taskListId = taskListId
    )
}

fun SubtaskEntity.toSubtask(): Subtask {
    return Subtask(
        id = id,
        title = title,
        parentId = parentId,
        position = position,
        isCompleted = isCompleted,
        taskListId = taskListId
    )
}

fun Subtask.toSubtaskEntity(): SubtaskEntity {
    return SubtaskEntity(
        id = id,
        title = title,
        parentId = parentId,
        position = position,
        isCompleted = isCompleted,
        pendingAction = PendingActions.NONE,
        lastModified = System.currentTimeMillis(),
        taskListId = taskListId
    )
}

fun SubtaskEntity.toTaskDto(): TaskDto {
    return TaskDto(
        id = id,
        title = title,
        status = isCompletedString,
        parent = parentId,
        position = position,
        updated = lastModified.toIsoDate(),
        deadline = null,
    )
}

fun SubtaskEntity.synchronize(lastModified: Long? = null): SubtaskEntity {
    return SubtaskEntity(
        id = id,
        title = title,
        parentId = parentId,
        position = position,
        isCompleted = isCompleted,
        pendingAction = PendingActions.NONE,
        lastModified = lastModified ?: System.currentTimeMillis(),
        taskListId = taskListId
    )
}

fun SubtaskEntity.changePendingAction(pendingAction: PendingActions): SubtaskEntity {
    return SubtaskEntity(
        id = id,
        title = title,
        parentId = parentId,
        position = position,
        isCompleted = isCompleted,
        pendingAction = pendingAction,
        lastModified = lastModified,
        taskListId = taskListId
    )
}

val SubtaskEntity.isCompletedString: String
    get() = if (isCompleted) "completed" else "needsAction"