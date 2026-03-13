package com.nikkap.calendar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tasklist")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    @ColumnInfo("title")
    val title: String
)