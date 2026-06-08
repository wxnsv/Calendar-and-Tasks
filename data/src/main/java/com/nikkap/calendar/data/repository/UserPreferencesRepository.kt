package com.nikkap.calendar.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nikkap.calendar.core.utils.toIsoDateWithoutSeconds
import com.nikkap.calendar.data.local.prefs.UserPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {

    private object Keys {
        val IS_AUTHORIZED = booleanPreferencesKey("is_authorized")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PHOTO_PATH = stringPreferencesKey("user_photo_path")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val CALENDAR_LAST_SYNC = longPreferencesKey("event_last_sync")
        val TASK_LAST_SYNC = longPreferencesKey("task_last_sync")
        val DEFAULT_TASKLIST_ID = stringPreferencesKey("default_tasklist_id")
        val CALENDAR_TIME_MIN = stringPreferencesKey("calendar_time_min")
        val IS_LIST_SCREEN_LAST = booleanPreferencesKey("is_list_screen_last")
        val IS_LAST_OPENED_SCREEN = booleanPreferencesKey("is_last_opened_screen")
        val IS_SYSTEM_THEME = booleanPreferencesKey("is_system_theme")
        val IS_LIGHT_THEME = booleanPreferencesKey("is_light_theme")
        val IS_MONDAY_FIRST_DAY = booleanPreferencesKey("is_monday_first_day")
        val IS_SYSTEM_FIRST_DAY = booleanPreferencesKey("is_system_first_day")
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
                photoPath = prefs[Keys.USER_PHOTO_PATH],
                isFirstLaunch = prefs[Keys.IS_FIRST_LAUNCH] ?: true,
                defaultTasklistId = prefs[Keys.DEFAULT_TASKLIST_ID],
                isListScreenLast = prefs[Keys.IS_LIST_SCREEN_LAST] ?: true,
                isLastOpenedSelected = prefs[Keys.IS_LAST_OPENED_SCREEN] ?: false,
                isSystemTheme = prefs[Keys.IS_SYSTEM_THEME] ?: true,
                isLightTheme = prefs[Keys.IS_LIGHT_THEME] ?: true,
                isMondayFirstDay = prefs[Keys.IS_MONDAY_FIRST_DAY] ?: true,
                isSystemFirstDay = prefs[Keys.IS_SYSTEM_FIRST_DAY] ?: true,
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

    val calendarTimeMin = dataStore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
    }
        .map { prefs ->
            prefs[Keys.CALENDAR_TIME_MIN]
        }

    val defaultTasklistId = dataStore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
    }
        .map { prefs ->
            prefs[Keys.DEFAULT_TASKLIST_ID]
        }

    suspend fun authorizeSession(email: String, name: String) {
        dataStore.edit { prefs ->
            prefs[Keys.USER_EMAIL] = email
            prefs[Keys.IS_AUTHORIZED] = true
            prefs[Keys.IS_FIRST_LAUNCH] = false
            prefs[Keys.USER_NAME] = name
        }
    }

    suspend fun updateTaskSyncTime() {
        dataStore.edit { prefs ->
            prefs[Keys.TASK_LAST_SYNC] = System.currentTimeMillis()
        }
    }

    suspend fun updateEventSyncTime(syncTime: Long = System.currentTimeMillis()) {
        dataStore.edit { prefs ->
            prefs[Keys.CALENDAR_LAST_SYNC] = syncTime
        }
    }

    suspend fun clearLastCalendarSyncTime() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.CALENDAR_LAST_SYNC)
        }
    }

    suspend fun clearLastTaskSyncTime() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.TASK_LAST_SYNC)
        }
    }

    suspend fun setDefaultTasklistId(id: String) {
        dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_TASKLIST_ID] = id
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

    suspend fun completeFirstLaunch() {
        dataStore.edit { prefs ->
            prefs[Keys.IS_FIRST_LAUNCH] = false
            prefs[Keys.CALENDAR_TIME_MIN] =
                Instant.now()
                    .atZone(ZoneOffset.UTC)
                    .minusYears(1)
                    .toInstant()
                    .toEpochMilli()
                    .toIsoDateWithoutSeconds()
        }
    }

    suspend fun saveUserPhoto(photoPath: String) {
        dataStore.edit { prefs ->
            prefs[Keys.USER_PHOTO_PATH] = photoPath
        }
    }

    suspend fun updateIsLastOpenedScreen(isLastOpenedScreen: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_LAST_OPENED_SCREEN] = isLastOpenedScreen
        }
    }

    suspend fun updateIsSystemTheme(isSystemTheme: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_SYSTEM_THEME] = isSystemTheme
        }
    }

    suspend fun updateIsLightTheme(isLightTheme: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_LIGHT_THEME] = isLightTheme
            prefs[Keys.IS_SYSTEM_THEME] = false
        }
    }

    suspend fun updateIsMondayFirstDay(isMondayFirstDay: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_MONDAY_FIRST_DAY] = isMondayFirstDay
            prefs[Keys.IS_SYSTEM_FIRST_DAY] = false
        }
    }

    suspend fun updateIsSystemFirstDay(isSystemFirstDay: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_SYSTEM_FIRST_DAY] = isSystemFirstDay
        }
    }

    suspend fun updateIsListScreenLast(boolean: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_LIST_SCREEN_LAST] = boolean
        }
    }

}