package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.TaskListEntity
import com.nikkap.calendar.data.remote.dto.TaskListDto
import com.nikkap.calendar.domain.model.TaskList

fun TaskListDto.toTaskListEntity(): TaskListEntity {
    return TaskListEntity(
        id = id,
        title = title,
        pendingAction = PendingActions.NONE,
        lastModified = parseIsoDate(updated)
    )
}

fun TaskListEntity.toTaskList(): TaskList {
    return TaskList(
        id = id,
        title = title
    )
}

fun TaskListEntity.toTaskListDto(): TaskListDto {
    return TaskListDto(
        id = id,
        title = title,
        updated = ""
    )
}

fun TaskListEntity.markAsSynchronized(
    lastModified: Long? = null,
    currentTime: Long = System.currentTimeMillis()
): TaskListEntity {
    return this.copy(
        pendingAction = PendingActions.NONE,
        lastModified = lastModified ?: currentTime
    )
}

fun TaskListEntity.changePendingAction(pendingAction: PendingActions): TaskListEntity {
    return this.copy(
        pendingAction = pendingAction,
    )
}