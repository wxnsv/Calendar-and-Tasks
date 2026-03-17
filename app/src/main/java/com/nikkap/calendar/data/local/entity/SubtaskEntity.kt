package com.nikkap.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "subtasks")
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    @ColumnInfo("title")
    val title: String?,
    @ColumnInfo("parentId")
    val parentId: String,
    @ColumnInfo("position")
    val position: String,
    @ColumnInfo("isCompleted")
    val isCompleted: Boolean,
    @ColumnInfo("isSynced")
    val isSynced: Boolean,
    @ColumnInfo("pendingAction")
    val pendingAction: PendingActions,
    @ColumnInfo("lastModified")
    val lastModified: Long,
)
