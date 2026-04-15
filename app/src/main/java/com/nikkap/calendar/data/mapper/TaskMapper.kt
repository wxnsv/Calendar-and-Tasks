package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.core.utils.toIsoDate
import com.nikkap.calendar.core.utils.toIsoDateWithoutTime
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
        due = deadline?.let { Instant.fromEpochMilliseconds(deadline) }.toString(),
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

fun Task.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = id!!,
        title = title,
        notes = notes,
        deadline = deadline,
        isCompleted = isCompleted,
        taskListId = taskListId,
        pendingAction = PendingActions.NONE,
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
    get() = due?.let { parseIsoDate(it, true) }
val TaskDto.isCompleted: Boolean
    get() = status == "completed"

val TaskEntity.isCompletedString: String
    get() = if (isCompleted) "completed" else "needsAction"