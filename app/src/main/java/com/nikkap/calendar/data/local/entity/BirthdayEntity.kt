package com.nikkap.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "birthday")
data class BirthdayEntity(
    @PrimaryKey(autoGenerate = false)
    override val id: String,
    @ColumnInfo("pendingAction")
    override val pendingAction: PendingActions,
    @ColumnInfo("lastModified")
    override val lastModified: Long,
    @ColumnInfo("name")
    val name: String?,
    @ColumnInfo("date")
    val date: Long,
    @ColumnInfo("colorId")
    val colorId: Int,
) : SyncableEntity