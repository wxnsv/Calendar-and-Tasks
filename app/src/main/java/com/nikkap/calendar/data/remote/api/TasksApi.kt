package com.nikkap.calendar.data.remote.api

import com.google.gson.annotations.SerializedName
import com.nikkap.calendar.data.remote.dto.TaskDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface TasksApi {

    @GET("tasks/v1/lists/@default/tasks")
    suspend fun getUserTasks(
        @Header("Authorization") token: String,
        @Query("showCompleted") showCompleted: Boolean = true,
        @Query("showHidden") showHidden: Boolean = true,
    ): Response<TasksListResponse>

    @DELETE("tasks/v1/lists/{taskListId}/tasks/{taskId}")
    suspend fun deleteTask(
        @Header("Authorization") authHeader: String,
        @Path("taskListId") taskListId: String = "@default",
        @Path("taskId") taskId: String
    ): Response<Unit>
}

// TODO("Send Tasks")
data class TasksListResponse(
    @SerializedName("items")
    val items: List<TaskDto>? = emptyList()
)