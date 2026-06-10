package com.nikkap.calendar.data.remote.api

import com.google.gson.annotations.SerializedName
import com.nikkap.calendar.data.remote.dto.TaskDto
import com.nikkap.calendar.data.remote.dto.TaskListDto
import com.nikkap.calendar.data.remote.dto.update.TaskListUpdateDto
import com.nikkap.calendar.data.remote.dto.update.TaskUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TasksApi {
    @GET("tasks/v1/lists/{taskListId}/tasks")
    suspend fun getTasks(
        @Path("taskListId") taskListId: String,
        @Query("showCompleted") showCompleted: Boolean = true,
        @Query("showHidden") showHidden: Boolean = true,
        @Query("showDeleted") showDeleted: Boolean = true,
    ): Response<TasksListResponse>

    @POST("tasks/v1/lists/{tasklist}/tasks")
    suspend fun createTask(
        @Path("tasklist") taskListId: String,
        @Body taskDto: TaskDto
    ): Response<TaskDto>

    @PATCH("tasks/v1/lists/{tasklistId}/tasks/{taskId}")
    suspend fun updateTask(
        @Path("tasklistId") taskListId: String,
        @Path("taskId") taskId: String,
        @Body task: TaskUpdateDto
    ): Response<TaskDto>

    @GET("tasks/v1/users/@me/lists")
    suspend fun getTaskLists(
        @Query("showDeleted") showDeleted: Boolean = true,
        @Query("maxResults") maxResults: Int? = 100,
    ): Response<TaskListsResponse>

    @GET("tasks/v1/users/@me/lists/{tasklistId}")
    suspend fun getTasklist(
        @Path("tasklistId") tasklistId: String = "@default"
    ): Response<TaskListDto>

    @DELETE("tasks/v1/lists/{taskListId}/tasks/{taskId}")
    suspend fun deleteTask(
        @Path("taskListId") taskListId: String,
        @Path("taskId") taskId: String
    ): Response<Unit>

    @POST("tasks/v1/users/@me/lists")
    suspend fun createTaskList(
        @Body taskList: TaskListDto
    ): Response<TaskListDto>

    @PUT("tasks/v1/users/@me/lists/{tasklist}")
    suspend fun updateTaskList(
        @Path("tasklist") taskListId: String,
        @Body taskList: TaskListUpdateDto
    ): Response<TaskListUpdateDto>

    @DELETE("tasks/v1/users/@me/lists/{tasklist}")
    suspend fun deleteTaskList(
        @Path("tasklist") taskListId: String
    ): Response<Unit>

    @POST("lists/{tasklist}/tasks")
    suspend fun createSubtask(
        @Path("tasklist") taskListId: String,
        @Body subtask: TaskUpdateDto
    ): Response<TaskUpdateDto>

    @PATCH("tasks/v1/lists/{tasklistId}/tasks/{subtaskId}")
    suspend fun updateSubtask(
        @Path("tasklistId") taskListId: String,
        @Path("subtaskId") subtaskId: String,
        @Body subtask: TaskUpdateDto
    ): Response<TaskUpdateDto>

    @DELETE("lists/{tasklist}/tasks/{task}")
    suspend fun deleteSubtask(
        @Path("tasklist") taskListId: String,
        @Path("task") subtaskId: String
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