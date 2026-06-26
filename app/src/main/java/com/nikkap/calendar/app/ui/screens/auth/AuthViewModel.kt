package com.nikkap.calendar.app.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.tasks.TasksScopes
import com.nikkap.calendar.app.core.auth.AuthentificationManager
import com.nikkap.calendar.app.core.auth.AuthorizationManager
import com.nikkap.calendar.data.local.prefs.UserPrefs
import com.nikkap.calendar.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AuthViewModel(
    private val authorizationManager: AuthorizationManager,
    private val userPrefRepository: UserPreferencesRepository,
    private val authentificationManager: AuthentificationManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())

    private val _prefsFlow = userPrefRepository.userStateFlow.map<UserPrefs, UserPrefs?> { it }
        .onStart { emit(null) }

    val state = combine(
        _state,
        _prefsFlow
    ) { state, userPrefs ->

        if (userPrefs != null) {

            val requiredScopes: MutableList<Scope> = mutableListOf()
            if (!userPrefs.isTasksGranted) requiredScopes.add(Scope(TasksScopes.TASKS))
            else requiredScopes.remove(Scope(TasksScopes.TASKS))
            if (!userPrefs.isProfileGranted) requiredScopes.add(Scope(Scopes.PROFILE))
            else requiredScopes.remove(Scope(Scopes.PROFILE))
            if (!userPrefs.isCalendarGranted) requiredScopes.add(Scope(CalendarScopes.CALENDAR_EVENTS))
            else requiredScopes.remove(Scope(CalendarScopes.CALENDAR_EVENTS))

            val isAllGranted =
                userPrefs.isCalendarGranted && userPrefs.isTasksGranted && userPrefs.isProfileGranted

            state.copy(
                isCalendarGranted = userPrefs.isCalendarGranted,
                isTasksGranted = userPrefs.isTasksGranted,
                isProfileGranted = userPrefs.isProfileGranted,
                isAllGranted = isAllGranted,
                requiredScopes = requiredScopes,
                isLoading = false,
                email = userPrefs.email
            )
        } else state.copy(isLoading = true)

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState()
    )

    fun startAuth(context: Context, authIntent: () -> Unit) {
        viewModelScope.launch {
            val userInfo = authentificationManager.authenticate(context)
            if (userInfo != null) {
                userPrefRepository.saveUserProfile(
                    userInfo.email,
                    userInfo.displayName ?: "",
                )
                _state.update {
                    it.copy(
                        email = if (it.email.isNullOrBlank()) userInfo.email else it.email,
                        photoUri = userInfo.photoUri ?: ""
                    )
                }
                authIntent()
            }
        }
    }

    fun onAuthIntentReady(
        intentSender: IntentSender,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val request = IntentSenderRequest.Builder(intentSender).build()
        launcher.launch(request)
    }

    fun handleLauncherResult(intent: Intent?) {
        viewModelScope.launch {
            authorizationManager.handleActivityResult(intent) { result ->
                viewModelScope.launch {
                    _state.update { it.copy(isFirstLaunch = false) }
                    val resultEmail = fetchEmailFromToken(result?.accessToken)
                    if (resultEmail != state.value.email && resultEmail != null) {
                        _state.update {
                            it.copy(
                                errorMessage = "Please select your primary account (${state.value.email}) to grant permissions."
                            )
                        }
                        return@launch
                    }
                    val scopes = result?.grantedScopes
                    if (scopes != null) {
                        if (scopes.contains("https://www.googleapis.com/auth/calendar.events")) {
                            userPrefRepository.calendarGranted()
                            _state.update { it.copy(isCalendarGranted = true) }
                        }
                        if (scopes.contains("https://www.googleapis.com/auth/tasks")) {
                            userPrefRepository.tasksGranted()
                            _state.update { it.copy(isTasksGranted = true) }
                        }
                        if (scopes.contains("https://www.googleapis.com/auth/userinfo.profile")) {
                            userPrefRepository.profileGranted()
                            _state.update { it.copy(isProfileGranted = true) }
                        }
                    }
                    _state.update { it.copy(isFirstLaunch = false) }
                }
            }
        }
    }

    private suspend fun fetchEmailFromToken(accessToken: String?): String? =
        withContext(Dispatchers.IO) {
            if (accessToken == null) return@withContext null
            try {
                val url = URL("https://www.googleapis.com/oauth2/v3/userinfo")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                connection.setRequestProperty("Authorization", "Bearer $accessToken")

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }

                    val jsonObject = JSONObject(response)
                    return@withContext jsonObject.optString("email")
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    fun invalidateCache() {
        viewModelScope.launch {
            authorizationManager.revokeToken()
            authentificationManager.revokeCredentials()
        }
    }

    fun resetScopes() {
        viewModelScope.launch {
            userPrefRepository.clearScopes()
        }
    }
}
