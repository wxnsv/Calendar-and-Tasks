package com.nikkap.calendar.data.local.prefs

data class UserPrefs(
    val isAuthorized: Boolean = false,
    val email: String? = null,
    val photoPath: String? = null,
    val name: String? = null,
    val isFirstLaunch: Boolean = true,
    val defaultTasklistId: String? = null,
    val isListScreenLast: Boolean = true,
    val isLastOpenedSelected: Boolean = true,
    val isSystemTheme: Boolean = true,
    val isLightTheme: Boolean = true,
    val isMondayFirstDay: Boolean = true,
    val isSystemFirstDay: Boolean = true,
    val isCalendarGranted: Boolean = false,
    val isTasksGranted: Boolean = false,
    val isProfileGranted: Boolean = false,
)