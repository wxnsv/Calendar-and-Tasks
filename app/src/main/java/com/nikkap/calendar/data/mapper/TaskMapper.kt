package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.data.local.entity.SubtaskEntity
import com.nikkap.calendar.data.local.entity.TaskEntity
import com.nikkap.calendar.data.local.entity.TaskListEntity
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.data.remote.dto.TaskListDto
import com.nikkap.calendar.domain.model.Subtask
import com.nikkap.calendar.domain.model.Task
import com.nikkap.calendar.domain.model.TaskList
import kotlinx.datetime.Instant

fun Task.toTaskDto(): TaskDto {
    return TaskDto(
        id = id,
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

fun Task.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        notes = notes,
        deadline = deadline,
        isCompleted = isCompleted,
        taskListId = taskListId,
    )
}

fun TaskDto.toTaskEntity(taskListId: String): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        notes = notes,
        isCompleted = isCompleted,
        deadline = dateLong,
        taskListId = taskListId
    )
}

fun TaskDto.toSubtaskEntity(): SubtaskEntity {
    return SubtaskEntity(
        id = id,
        title = title,
        parentId = parent!!,
        position = position.toString(),
        isCompleted = isCompleted
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