package com.nikkap.calendar.app.core.auth

import android.content.IntentSender
import com.google.android.gms.auth.api.identity.AuthorizationResult

sealed interface AuthorizationManagerResult {
    data class NeedResolution(val intentSender: IntentSender) : AuthorizationManagerResult
    data class Success(val result: AuthorizationResult) : AuthorizationManagerResult
    object InvalidCache : AuthorizationManagerResult
}