package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.toIsoDate
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.TaskEntity
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.domain.model.Task
import kotlin.time.Instant

fun Task.toTaskDto(): TaskDto {
    return TaskDto(
        id = id!!,
        title = title,
        status = if (isCompleted) "completed" else "needsAction",
        notes = notes,
        deadline = deadline?.let { Instant.fromEpochMilliseconds(deadline) }.toString(),
    )
}
fun TaskDto.toTask(): Task {
    return Task(
        id = id,
        title = title,
        notes = notes,
        deadline = dateLong,
        isCompleted = isCompleted
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

fun Task.toTaskEntity(pendingAction: PendingActions): TaskEntity {
    return TaskEntity(
        id = id!!,
        title = title,
        notes = notes,
        deadline = deadline,
        isCompleted = isCompleted,
        taskListId = taskListId,
        pendingAction = pendingAction,
        lastModified = System.currentTimeMillis(),
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

fun TaskDto.toDeletedTaskEntity(taskListId: String): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        notes = notes,
        isCompleted = isCompleted,
        deadline = dateLong,
        taskListId = taskListId,
        pendingAction = PendingActions.DELETE,
        lastModified = parseIsoDate(updated)
    )
}

fun TaskEntity.toTaskDto(): TaskDto {
    return TaskDto(
        id = id,
        title = title,
        notes = notes,
        status = isCompletedString,
        deadline = deadline?.toIsoDate(), // TODO (IS ALL DAY?)
        parent = null,
        position = null,
        updated = lastModified.toIsoDate(),
    )
}

fun TaskEntity.synchronize(lastModified: Long? = null): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        notes = notes,
        isCompleted = isCompleted,
        deadline = deadline,
        taskListId = taskListId,
        pendingAction = PendingActions.NONE,
        lastModified = lastModified ?: System.currentTimeMillis()
    )
}

fun TaskEntity.changePendingAction(pendingAction: PendingActions): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        notes = notes,
        isCompleted = isCompleted,
        deadline = deadline,
        taskListId = taskListId,
        pendingAction = pendingAction,
        lastModified = lastModified
    )
}


val TaskDto.dateLong: Long?
    get() = deadline?.let { parseIsoDate(it) }
val TaskDto.isCompleted: Boolean
    get() = status == "completed"

val TaskEntity.isCompletedString: String
    get() = if (isCompleted) "completed" else "needsAction"