package com.nikkap.calendar.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nikkap.calendar.data.local.prefs.UserPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {

    private object Keys {
        val IS_AUTHORIZED = booleanPreferencesKey("is_authorized")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    val userStateFlow: Flow<UserPrefs> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            UserPrefs(
                isAuthorized = prefs[Keys.IS_AUTHORIZED] ?: false,
                lastSyncTime = prefs[Keys.LAST_SYNC_TIMESTAMP],
                email = prefs[Keys.USER_EMAIL],
                name = prefs[Keys.USER_NAME]
            )
        }

    suspend fun saveSession(email: String, name: String) {
        dataStore.edit { prefs ->
            prefs[Keys.USER_EMAIL] = email
            prefs[Keys.IS_AUTHORIZED] = true
            prefs[Keys.USER_NAME] = name
        }
    }

    suspend fun updateSyncTime() {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

}