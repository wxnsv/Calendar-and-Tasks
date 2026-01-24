package com.nikkap.calendar.core.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.tasks.TasksScopes
import kotlinx.coroutines.tasks.await

class AuthManager(context: Context) {
    private val authClient = Identity.getAuthorizationClient(context)
    private val credentialManager = CredentialManager.create(context)

    private val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID"
    private val REQUESTEDSCOPES = listOf(
        Scope(TasksScopes.TASKS),
        Scope(CalendarScopes.CALENDAR_EVENTS)
    )

    private fun getAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.builder()
            .setRequestedScopes(REQUESTEDSCOPES)
//            .requestOfflineAccess(WEB_CLIENT_ID, true)
            .build()
    }

    fun handleActivityResult(intent: Intent?, onResult: (String?) -> Unit) {
        try {
            val result = authClient.getAuthorizationResultFromIntent(intent)
            onResult(result.accessToken)
        } catch (e: Exception) {
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
            }
            .addOnFailureListener(onFailure)
    }

    suspend fun getAccessToken(): String? {
        return try {
            val result = authClient.authorize(getAuthorizationRequest()).await()
            result.accessToken
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        try {
            authClient.revokeAccess(RevokeAccessRequest.builder().build()).await()
        } catch (e: Exception) {
            Log.d("Authorization", "Logout exception ${e.message}")
//            TODO("extensive exception handling")
        }
    }
}