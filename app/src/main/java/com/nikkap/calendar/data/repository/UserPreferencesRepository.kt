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
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHOTO = stringPreferencesKey("user_photo")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val CALENDAR_LAST_SYNC = longPreferencesKey("event_last_sync")
        val TASK_LAST_SYNC = longPreferencesKey("task_last_sync")
    }

    val userStateFlow: Flow<UserPrefs> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            UserPrefs(
                isAuthorized = prefs[Keys.IS_AUTHORIZED] ?: false,
                eventLastSync = prefs[Keys.CALENDAR_LAST_SYNC],
                taskLastSync = prefs[Keys.TASK_LAST_SYNC],
                email = prefs[Keys.USER_EMAIL],
                name = prefs[Keys.USER_NAME],
                isFirstLaunch = prefs[Keys.IS_FIRST_LAUNCH] ?: true
            )
        }

    val taskSyncTime: Flow<Long?> = dataStore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
    }
        .map { prefs ->
            prefs[Keys.TASK_LAST_SYNC]
        }

    val calendarSyncTime: Flow<Long?> = dataStore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
    }
        .map { prefs ->
            prefs[Keys.CALENDAR_LAST_SYNC]
        }

    suspend fun authorizeSession(email: String, name: String, photoUri: String) {
        dataStore.edit { prefs ->
            prefs[Keys.USER_EMAIL] = email
            prefs[Keys.IS_AUTHORIZED] = true
            prefs[Keys.IS_FIRST_LAUNCH] = false
            prefs[Keys.USER_NAME] = name
            prefs[Keys.USER_PHOTO] = photoUri
        }
    }

    suspend fun updateTaskSyncTime() {
        dataStore.edit { prefs ->
            prefs[Keys.TASK_LAST_SYNC] = System.currentTimeMillis()
        }
    }

    suspend fun updateEventSyncTime() {
        dataStore.edit { prefs ->
            prefs[Keys.CALENDAR_LAST_SYNC] = System.currentTimeMillis()
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

    suspend fun completeFirstLaunch() {
        dataStore.edit { prefs ->
            prefs[Keys.IS_FIRST_LAUNCH] = false
        }
    }


}