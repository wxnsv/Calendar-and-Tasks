package com.nikkap.calendar.core.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.tasks.TasksScopes
import kotlinx.coroutines.tasks.await

class AuthorizationManager(context: Context) {
    private val authClient = Identity.getAuthorizationClient(context)

    private val requestedScopes = listOf(
        Scope(TasksScopes.TASKS),
        Scope(CalendarScopes.CALENDAR_EVENTS),
        Scope(Scopes.EMAIL),
        Scope(Scopes.PROFILE)
    )

    private fun getAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .build()
    }

    fun handleActivityResult(intent: Intent?, onResult: (String?) -> Unit) {
        try {
            val result = authClient.getAuthorizationResultFromIntent(intent)
            onResult(result.accessToken)
        } catch (_: Exception) {
            onResult(null)
        }
    }

    fun getAuthIntent(onPendingIntent: (IntentSender) -> Unit) {
        authClient.authorize(getAuthorizationRequest()).addOnSuccessListener { result ->
            if (result.hasResolution()) {
                onPendingIntent(result.pendingIntent!!.intentSender)
            }
        }
    }

    suspend fun getAccessToken(): String? {
        return try {
            val result = authClient.authorize(getAuthorizationRequest()).await()
            result.accessToken
        } catch (_: Exception) {
            null
        }
    }
}