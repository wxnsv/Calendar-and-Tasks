package com.nikkap.calendar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nikkap.calendar.data.local.entity.BirthdayEntity
import com.nikkap.calendar.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM event")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM birthday")
    fun getAllBirthdays(): Flow<List<BirthdayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(eventEntities: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthdays(birthdayEntities: List<BirthdayEntity>)

    @Query("SELECT COUNT(*) FROM event")
    suspend fun getCount(): Int

    @Query("SELECT * from event WHERE id = :id")
    suspend fun getEvent(id: String): EventEntity

    @Query("SELECT * from birthday WHERE id = :id")
    suspend fun getBirthday(id: String): BirthdayEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(eventEntity: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthday(birthdayEntity: BirthdayEntity)

    @Update
    suspend fun updateEvent(eventEntity: EventEntity)
}