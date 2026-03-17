package com.nikkap.calendar.data.remote.api

import com.google.gson.annotations.SerializedName
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.data.remote.dto.TaskListDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TasksApi {

    @GET("tasks/v1/lists/{taskListId}/tasks")
    suspend fun getTasks(
        @Path("taskListId") taskListId: String,
        @Query("showCompleted") showCompleted: Boolean = true,
        @Query("showHidden") showHidden: Boolean = true,
    ): Response<TasksListResponse>

    @POST("tasks/v1/lists/{tasklist}/tasks")
    suspend fun createTask(
        @Path("tasklist") taskListId: String = "@default",
        @Body taskDto: TaskDto
    ): Response<TaskDto>

    @PATCH("tasks/v1/lists/{tasklist}/tasks/{task}")
    suspend fun updateTask(
        @Path("tasklist") taskListId: String = "@default",
        @Path("task") taskId: String,
        @Body task: TaskDto
    ): Response<TaskDto>

    @GET("tasks/v1/users/@me/lists")
    suspend fun getTaskLists(
    ): Response<TaskListsResponse>

    @DELETE("tasks/v1/lists/{taskListId}/tasks/{taskId}")
    suspend fun deleteTask(
        @Path("taskListId") taskListId: String = "@default",
        @Path("taskId") taskId: String
    ): Response<Unit>
}

data class TasksListResponse(
    @SerializedName("items")
    val items: List<TaskDto>? = emptyList()
)

data class TaskListsResponse(
    @SerializedName("items")
    val items: List<TaskListDto>? = emptyList()
)