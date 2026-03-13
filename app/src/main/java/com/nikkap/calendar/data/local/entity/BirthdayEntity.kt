package com.nikkap.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "birthday")
data class BirthdayEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    @ColumnInfo("name")
    val name: String?,
    @ColumnInfo("date")
    val date: Long,
)
