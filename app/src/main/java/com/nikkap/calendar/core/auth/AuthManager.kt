package com.nikkap.calendar.core.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.tasks.TasksScopes
import kotlinx.coroutines.tasks.await

class AuthManager(context: Context) {
    private val authClient = Identity.getAuthorizationClient(context)

    private val requestedScopes = listOf(
        Scope(TasksScopes.TASKS),
        Scope(CalendarScopes.CALENDAR_EVENTS)
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

    fun silentAuthorize(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        authClient.authorize(getAuthorizationRequest())
            .addOnSuccessListener { result ->
                result.accessToken?.let(onSuccess) ?: onFailure(Exception("No token"))
                Log.d("AppAuth", "Silent Authorize is Success")
            }
            .addOnFailureListener {
                Log.d("AppAuth", "Silent Authorize is Failed")
                onFailure(Exception("Silent Authorize is failed"))
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