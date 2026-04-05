package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.toRfc3339
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.TaskListEntity
import com.nikkap.calendar.data.remote.dto.TaskListDto
import com.nikkap.calendar.domain.model.TaskList

fun TaskListDto.toTaskList(): TaskList {
    return TaskList(
        id = id,
        title = title
    )
}

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
        updated = lastModified.toRfc3339()!!
    )
}

fun TaskListEntity.synchronize(lastModified: Long? = null): TaskListEntity {
    return TaskListEntity(
        id = id,
        title = title,
        pendingAction = PendingActions.NONE,
        lastModified = lastModified ?: System.currentTimeMillis()
    )
}

fun TaskListEntity.changePendingAction(pendingAction: PendingActions): TaskListEntity {
    return TaskListEntity(
        id = id,
        title = title,
        pendingAction = pendingAction,
        lastModified = lastModified
    )
}