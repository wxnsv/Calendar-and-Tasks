package com.nikkap.calendar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.nikkap.calendar.data.local.entity.CalendarItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar")
    fun getAllEvents(): Flow<List<CalendarItemEntity>>

    @Upsert
    suspend fun upsertEvents(events: List<CalendarItemEntity>)

    @Insert
    suspend fun insertEvents(events: List<CalendarItemEntity>)

    @Query("DELETE FROM calendar")
    suspend fun clearAll()
}