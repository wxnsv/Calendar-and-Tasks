package com.nikkap.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    @ColumnInfo("title")
    val title: String?,
    @ColumnInfo("notes")
    val notes: String?,
    @ColumnInfo("isCompleted")
    val isCompleted: Boolean,
    @ColumnInfo("date")
    val deadline: Long?,
    @ColumnInfo("taskListId")
    val taskListId: String,
    // TODO(Reminders)
)
