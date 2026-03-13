package com.nikkap.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event")
data class EventEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    @ColumnInfo("summary")
    val summary: String?,
    @ColumnInfo("description")
    val description: String?,
    @ColumnInfo("startTimestamp")
    val startTimestamp: Long,
    @ColumnInfo("endTimestamp")
    val endTimestamp: Long?,
    @ColumnInfo("isAllDay")
    val isAllDay: Boolean,
    @ColumnInfo("colorHex")
    val colorHex: String?,
    @ColumnInfo("status")
    val status: String?,
)


