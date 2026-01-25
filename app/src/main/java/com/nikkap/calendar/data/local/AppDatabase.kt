package com.nikkap.calendar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nikkap.calendar.data.local.dao.CalendarDao
import com.nikkap.calendar.data.local.dao.TaskDao
import com.nikkap.calendar.data.local.entity.CalendarItemEntity
import com.nikkap.calendar.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        CalendarItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun calendarDao(): CalendarDao
}