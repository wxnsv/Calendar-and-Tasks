package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.toIsoDate
import com.nikkap.calendar.core.utils.toIsoDateWithoutTime
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.TaskEntity
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.domain.model.Task

fun Task.toTaskDto(): TaskDto {
    return TaskDto(
        id = id!!,
        title = title,
        status = if (isCompleted) "completed" else "needsAction",
        notes = notes,
        due = deadline.toIsoDate(),
    )
}

fun TaskEntity.toTask(): Task {
    return Task(
        id = id,
        title = title ?: "(No title)",
        notes = notes,
        deadline = deadline,
        isCompleted = isCompleted
    )
}

fun Task.toTaskEntity(currentTime: Long = System.currentTimeMillis()): TaskEntity {
    return TaskEntity(
        id = id!!,
        title = title,
        notes = notes,
        deadline = deadline,
        isCompleted = isCompleted,
        taskListId = taskListId,
        pendingAction = PendingActions.NONE,
        lastModified = currentTime,
    )
}

fun TaskDto.toTaskEntity(taskListId: String): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        notes = notes,
        isCompleted = isCompleted,
        deadline = dateLong,
        taskListId = taskListId,
        pendingAction = PendingActions.NONE,
        lastModified = parseIsoDate(updated)
    )
}

fun TaskEntity.toTaskDto(): TaskDto {
    return TaskDto(
        id = id,
        title = title,
        notes = notes,
        status = isCompletedString,
        due = deadline?.toIsoDateWithoutTime(),
        parent = null,
        position = null, // TODO check
    )
}

fun TaskEntity.markAsSynchronized(
    lastModified: Long? = null,
    currentTime: Long = System.currentTimeMillis()
): TaskEntity {
    return this.copy(
        pendingAction = PendingActions.NONE,
        lastModified = lastModified ?: currentTime
    )
}

fun TaskEntity.changePendingAction(pendingAction: PendingActions): TaskEntity {
    return this.copy(
        pendingAction = pendingAction,
    )
}


val TaskDto.dateLong: Long?
    get() = due?.let { parseIsoDate(it, true) }
val TaskDto.isCompleted: Boolean
    get() = status == "completed"

val TaskEntity.isCompletedString: String
    get() = if (isCompleted) "completed" else "needsAction"