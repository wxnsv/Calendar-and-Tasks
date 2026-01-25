package com.nikkap.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar")
data class CalendarItemEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    @ColumnInfo("summary")
    val summary: String,
    @ColumnInfo("startDate")
    val startDate: String?,
)

