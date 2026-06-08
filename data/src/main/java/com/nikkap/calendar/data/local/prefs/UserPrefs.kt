package com.nikkap.calendar.data.local.prefs

data class UserPrefs(
    val isAuthorized: Boolean = false,
    val eventLastSync: Long? = null,
    val birthdayLastSync: Long? = null,
    val taskLastSync: Long? = null,
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
    val isSystemFirstDay: Boolean = true
)