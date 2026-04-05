package com.nikkap.calendar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nikkap.calendar.data.local.entity.BirthdayEntity
import com.nikkap.calendar.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {

    /**
     * EVENTS
     */

    @Query("SELECT * FROM event WHERE pendingAction != 'DELETE'")
    fun getNonDeleteEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event WHERE pendingAction != 'NONE'")
    fun getPendingEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(eventEntities: List<EventEntity>)

    @Delete
    suspend fun deleteEvent(eventEntity: EventEntity)

    @Query("SELECT * from event WHERE id = :id")
    suspend fun getEvent(id: String): EventEntity?

    @Query("DELETE FROM event WHERE id IN (:ids)")
    suspend fun deleteEventsByIds(ids: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(eventEntity: EventEntity)

    @Update
    suspend fun updateEvent(eventEntity: EventEntity)

    /**
     * BIRTHDAYS
     */

    @Query("SELECT * FROM birthday WHERE pendingAction != 'DELETE'")
    fun getNonDeleteBirthdays(): Flow<List<BirthdayEntity>>

    @Query("SELECT * FROM birthday WHERE pendingAction != 'NONE'")
    fun getPendingBirthdays(): Flow<List<BirthdayEntity>>

    @Query("DELETE FROM birthday")
    suspend fun clearBirthdays()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthdays(birthdayEntities: List<BirthdayEntity>)

    @Delete
    suspend fun deleteBirthday(birthdayEntity: BirthdayEntity)

    @Query("DELETE FROM birthday WHERE id IN (:ids)")
    suspend fun deleteBirthdaysByIds(ids: List<String>)

    @Query("SELECT * from birthday WHERE id = :id")
    suspend fun getBirthday(id: String): BirthdayEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthday(birthdayEntity: BirthdayEntity)

    @Update
    suspend fun updateBirthday(birthdayEntity: BirthdayEntity)

}