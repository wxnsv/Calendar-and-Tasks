package com.nikkap.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tasklist")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = false)
    override val id: String,
    @ColumnInfo("pendingAction")
    override val pendingAction: PendingActions,
    @ColumnInfo("lastModified")
    override val lastModified: Long,
    @ColumnInfo("title")
    val title: String,
) : SyncableEntity