package com.nikkap.calendar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
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

    /**
     * TASKS
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("SELECT * from tasks WHERE id = :id")
    suspend fun getTask(id: String): TaskEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(taskEntity: TaskEntity)

    @Update
    suspend fun updateTask(taskEntity: TaskEntity)

    @Delete
    suspend fun deleteTask(taskEntity: TaskEntity)

    @Query("DELETE FROM tasklist WHERE id IN (:ids)")
    suspend fun deleteTaskListsByIds(ids: List<String>)

    @Query("DELETE FROM tasks WHERE id IN (:ids)")
    suspend fun deleteTasksByIds(ids: List<String>)

    @Query("SELECT * FROM tasks WHERE pendingAction != 'DELETE'")
    fun getNonDeleteTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE pendingAction != 'NONE'")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    /**
     * SUBTASKS
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(tasks: List<SubtaskEntity>)

    @Query("DELETE FROM subtasks")
    suspend fun clearSubtasks()

    @Query("SELECT * from subtasks WHERE id = :id")
    suspend fun getSubtask(id: String): SubtaskEntity

    @Query("DELETE FROM subtasks WHERE id IN (:ids)")
    suspend fun deleteSubtasksByIds(ids: List<String>)

    @Query("SELECT * FROM subtasks WHERE pendingAction != 'DELETE'")
    fun getNonDeleteSubtasks(): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE pendingAction != 'NONE'")
    fun getPendingSubtasks(): Flow<List<SubtaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtaskEntity: SubtaskEntity)

    @Update
    suspend fun updateSubtask(subtaskEntity: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtaskEntity: SubtaskEntity)

    /**
     * TASKLISTS
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskLists(taskLists: List<TaskListEntity>)

    @Query("SELECT * from tasklist WHERE pendingAction != 'DELETE'")
    fun getNonDeleteTaskLists(): Flow<List<TaskListEntity>>

    @Query("SELECT * from tasklist WHERE pendingAction != 'NONE'")
    fun getPendingTaskLists(): Flow<List<TaskListEntity>>

    @Update
    suspend fun updateTasklist(taskList: TaskListEntity)

    @Delete
    suspend fun deleteTasklist(taskList: TaskListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskList(taskList: TaskListEntity)

}