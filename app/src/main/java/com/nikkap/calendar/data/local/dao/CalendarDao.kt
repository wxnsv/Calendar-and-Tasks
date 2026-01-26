package com.nikkap.calendar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nikkap.calendar.data.local.entity.CalendarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar")
    fun getAllItems(): Flow<List<CalendarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(calendarItems: List<CalendarEntity>)

    @Query("DELETE FROM calendar")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getCount(): Int
}