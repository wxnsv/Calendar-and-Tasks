package com.nikkap.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "subtasks")
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = false)
    override val id: String,
    @ColumnInfo("pendingAction")
    override val pendingAction: PendingActions,
    @ColumnInfo("lastModified")
    override val lastModified: Long,
    @ColumnInfo("title")
    val title: String?,
    @ColumnInfo("parentId")
    val parentId: String,
    @ColumnInfo("position")
    val position: String,
    @ColumnInfo("isCompleted")
    val isCompleted: Boolean,
    @ColumnInfo("taskListId")
    val taskListId: String,
    @ColumnInfo("deadline")
    val deadline: Long?,
) : SyncableEntity
