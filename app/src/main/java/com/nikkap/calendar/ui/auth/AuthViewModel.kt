package com.nikkap.calendar.ui.auth

import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import com.nikkap.calendar.core.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state = _state.asStateFlow()

    fun checkAuth() {
        authManager.silentAuthorize(
            onSuccess = { _state.value = AuthState.Authenticated },
            onFailure = { _state.value = AuthState.Unauthenticated }
        )
    }

    fun onAuthIntentReady(
        intentSender: IntentSender,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val request = IntentSenderRequest.Builder(intentSender).build()
        launcher.launch(request)
    }

    fun handleAuthResult(intent: Intent?) {
        authManager.handleActivityResult(intent) { token ->
            if (token != null) {
                _state.value = AuthState.Authenticated
            } else {
                _state.value = AuthState.Unauthenticated
            }
        }
    }
}
