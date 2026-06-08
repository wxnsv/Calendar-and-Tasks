package com.nikkap.calendar.app.ui.screens.auth

import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikkap.calendar.app.core.auth.AuthentificationManager
import com.nikkap.calendar.app.core.auth.AuthorizationManager
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authorizationManager: AuthorizationManager,
    private val userPrefRepository: UserPreferencesRepository,
    private val authentificationManager: AuthentificationManager
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state = _state.asStateFlow()
    private val _photoUri = MutableStateFlow("")
    val photoUri = _photoUri.asStateFlow()
    fun startAuth(authIntent: () -> Unit) {
        viewModelScope.launch {
            val userInfo = authentificationManager.authenticate()
            if (userInfo != null) {
                userPrefRepository.authorizeSession(
                    userInfo.email,
                    userInfo.displayName ?: "",
                )
                _photoUri.value = userInfo.photoUri ?: ""
                authIntent()
            }
        }
    }

    fun saveUserPhotoPath(path: String) {
        viewModelScope.launch {
            userPrefRepository.saveUserPhoto(path)
        }
    }

    fun onAuthIntentReady(
        intentSender: IntentSender,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val request = IntentSenderRequest.Builder(intentSender).build()
        launcher.launch(request)
    }

    fun handleAuthResult(intent: Intent?) {
        viewModelScope.launch {
            authorizationManager.handleActivityResult(intent) { token ->
                if (token != null) {
                    _state.value = AuthState.Authenticated
                } else {
                    _state.value = AuthState.Unauthenticated
                }
            }
        }
    }
}
