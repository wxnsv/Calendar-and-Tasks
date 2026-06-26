package com.nikkap.calendar.app.core.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.tasks.TasksScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class AuthorizationManager(context: Context) {
    private val authClient = Identity.getAuthorizationClient(context)

    private val requestedScopes = listOf(
        Scope(TasksScopes.TASKS),
        Scope(CalendarScopes.CALENDAR_EVENTS),
        Scope(Scopes.EMAIL),
        Scope(Scopes.PROFILE)
    )

    private fun getAuthorizationRequest(requestedScopes: List<Scope>): AuthorizationRequest {
        return AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .build()
    }

    fun handleActivityResult(intent: Intent?, onResult: (AuthorizationResult?) -> Unit) {
        try {
            val result = authClient.getAuthorizationResultFromIntent(intent)
            onResult(result)
        } catch (_: Exception) {
            onResult(null)
        }
    }

    fun getAuthResult(
        requestedScopes: List<String>,
        onResult: (AuthorizationManagerResult) -> Unit
    ) {
        val scopes = requestedScopes.map { Scope(it) }
        authClient.authorize(getAuthorizationRequest(scopes))
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    onResult(AuthorizationManagerResult.NeedResolution(result.pendingIntent!!.intentSender))
                } else {
                    val token = result.accessToken
                    if (token != null) {
                        onResult(AuthorizationManagerResult.Success(result))
                    } else {
                        onResult(AuthorizationManagerResult.InvalidCache)
                    }
                }
            }
    }

    suspend fun getAccessToken(): String? {
        return try {
            val result = authClient.authorize(
                getAuthorizationRequest(
                    requestedScopes
                )
            ).await()
            result.accessToken
        } catch (_: Exception) {
            null
        }
    }

    suspend fun revokeToken() = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            val url = URL("https://oauth2.googleapis.com/revoke?token=$accessToken")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}