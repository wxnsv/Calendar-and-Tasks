package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.SubtaskEntity
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.domain.model.Subtask

fun TaskDto.toSubtaskEntity(): SubtaskEntity {
    return SubtaskEntity(
        id = id,
        title = title,
        parentId = parent!!,
        position = position.toString(),
        isCompleted = isCompleted,
        isSynced = true,
        pendingAction = PendingActions.NONE,
        lastModified = parseIsoDate(updated)
    )
}

fun SubtaskEntity.toSubtask(): Subtask {
    return Subtask(
        id = id,
        title = title,
        parentId = parentId,
        position = position,
        isCompleted = isCompleted
    )
}

fun Subtask.toSubtaskEntity(pendingAction: PendingActions): SubtaskEntity {
    return SubtaskEntity(
        id = id,
        title = title,
        parentId = parentId,
        position = position,
        isCompleted = isCompleted,
        isSynced = false,
        pendingAction = pendingAction,
        lastModified = System.currentTimeMillis()
    )
}