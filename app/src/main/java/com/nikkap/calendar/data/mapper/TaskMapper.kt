package com.nikkap.calendar.data.mapper

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

fun TaskDto.toTask(): Task {
    return Task(
        id = this.id,
        title = this.title ?: "",
        notes = this.notes,
        date = this.date?.let { Instant.parse(it) }?.toEpochMilliseconds(),
        isCompleted = if (this.status == "completed") true else false,
        updated = runCatching { Instant.parse(this.updated).toEpochMilliseconds() }
            .getOrDefault(System.currentTimeMillis())
    )
}

fun TaskEntity.toTask(): Task {
    return Task(
        id = this.id,
        title = this.title,
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