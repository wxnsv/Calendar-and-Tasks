package com.nikkap.calendar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nikkap.calendar.data.local.dao.CalendarDao
import com.nikkap.calendar.data.local.dao.TaskDao
import com.nikkap.calendar.data.local.entity.BirthdayEntity
import com.nikkap.calendar.data.local.entity.EventEntity
import com.nikkap.calendar.data.local.entity.SubtaskEntity
import com.nikkap.calendar.data.local.entity.TaskEntity
import com.nikkap.calendar.data.local.entity.TaskListEntity

@Database(
    entities = [
        TaskEntity::class,
        EventEntity::class,
        BirthdayEntity::class,
        TaskListEntity::class,
        SubtaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun calendarDao(): CalendarDao
}