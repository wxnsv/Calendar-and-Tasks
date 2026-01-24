package com.nikkap.calendar.data.remote.dto

import com.nikkap.calendar.domain.model.Task

data class TaskDto(
    val id: String,
    val title: String,
    val status: String? = null,
    val notes: String? = null
)
//TODO("Correct class")

fun Task.toTaskDto(): TaskDto {
    return TaskDto(
        id = this.id,
        title = this.title,
        status = this.status,
        notes = this.notes
    )
}

fun TaskDto.toTask(): Task {
    return Task(
        id = this.id,
        title = this.title,
        status = this.status,
        notes = this.notes
    )
}