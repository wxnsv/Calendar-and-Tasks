package com.nikkap.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = false)
    override val id: String,
    @ColumnInfo("pendingAction")
    override val pendingAction: PendingActions,
    @ColumnInfo("lastModified")
    override val lastModified: Long,
    @ColumnInfo("title")
    val title: String?,
    @ColumnInfo("notes")
    val notes: String?,
    @ColumnInfo("isCompleted")
    val isCompleted: Boolean,
    @ColumnInfo("date")
    val deadline: Long?,
    @ColumnInfo("taskListId")
    val taskListId: String

    // TODO(Reminders)
) : SyncableEntity

enum class PendingActions { NONE, INSERT, UPDATE, DELETE }