package com.nikkap.calendar.data.mapper

import com.nikkap.calendar.core.utils.parseIsoDate
import com.nikkap.calendar.data.local.entity.TaskEntity
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.domain.model.Task
import kotlinx.datetime.Instant

fun Task.toTaskDto(): TaskDto {
    return TaskDto(
        id = this.id,
        title = this.title,
        status = if (this.isCompleted) "completed" else "needsAction",
        notes = this.notes,
        date = this.date?.let { Instant.fromEpochMilliseconds(this.date) }.toString(),
        updated = Instant.fromEpochMilliseconds(this.updated).toString()
    )
}
// TODO("Replace transformations to extend")

fun TaskDto.toTask(): Task {
    return Task(
        id = this.id,
        title = this.title,
        notes = this.notes,
        date = this.dateLong,
        isCompleted = isCompleted,
        updated = updatedLong
    )
}

fun TaskEntity.toTask(): Task {
    return Task(
        id = this.id,
        title = this.title ?: "(No title)",
        notes = this.notes,
        date = this.date,
        isCompleted = this.isCompleted,
        updated = this.updated
    )
}

fun Task.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = this.id,
        title = this.title,
        notes = this.notes,
        date = this.date,
        isCompleted = this.isCompleted,
        updated = this.updated
    )
}

fun TaskDto.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = this.id,
        title = this.title,
        notes = this.notes,
        isCompleted = this.isCompleted,
        date = this.dateLong,
        updated = this.updatedLong
    )
}

val TaskDto.dateLong: Long?
    get() = this.date?.let { parseIsoDate(it) }
val TaskDto.isCompleted: Boolean
    get() = this.status == "completed"
val TaskDto.updatedLong: Long
    get() = parseIsoDate(this.updated)