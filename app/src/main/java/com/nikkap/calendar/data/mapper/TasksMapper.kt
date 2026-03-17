package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.data.local.entity.PendingActions
import com.nikkap.calendar.data.local.entity.TaskEntity
import com.nikkap.calendar.data.local.entity.TaskListEntity
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.data.remote.dto.TaskListDto
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.model.TaskList

fun Task.toTaskDto(): TaskDto {
    return TaskDto(
        id = id!!,
        title = title,
        status = if (isCompleted) "completed" else "needsAction",
        notes = notes,
        deadline = deadline?.let { kotlin.time.Instant.fromEpochMilliseconds(deadline) }.toString(),
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
        isSynced = false,
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
        isSynced = true,
        pendingAction = PendingActions.NONE,
        lastModified = parseIsoDate(updated)
    )
}



fun TaskListDto.toTaskList(): TaskList {
    return TaskList(
        id = id,
        title = title
    )
}

fun TaskListDto.toTaskListEntity(): TaskListEntity {
    return TaskListEntity(
        id = id,
        title = title
    )
}

fun TaskListEntity.toTaskList(): TaskList {
    return TaskList(
        id = id,
        title = title
    )
}

val TaskDto.dateLong: Long?
    get() = deadline?.let { parseIsoDate(it) }
val TaskDto.isCompleted: Boolean
    get() = status == "completed"