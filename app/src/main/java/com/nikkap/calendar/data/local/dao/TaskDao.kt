package com.nikkap.calendar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nikkap.calendar.data.local.entity.SubtaskEntity
import com.nikkap.calendar.data.local.entity.TaskEntity
import com.nikkap.calendar.data.local.entity.TaskListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(tasks: List<SubtaskEntity>)
    @Query("DELETE FROM tasks")
    suspend fun clearTasks()
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getCount(): Int

    @Query("SELECT * from tasks WHERE id = :id")
    suspend fun getTask(id: String): TaskEntity

    @Query("SELECT * from subtasks WHERE id = :id")
    suspend fun getSubtask(id: String): SubtaskEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(taskEntity: TaskEntity)

    @Update
    suspend fun updateTask(taskEntity: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskLists(taskLists: List<TaskListEntity>)

    @Query("SELECT * from tasklist")
    fun getTaskLists(): Flow<List<TaskListEntity>>

    @Query("SELECT * FROM tasks WHERE pendingAction != 'DELETE'")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM subtasks WHERE pendingAction != 'DELETE'")
    fun getAllSubtasks(): Flow<List<SubtaskEntity>>
}