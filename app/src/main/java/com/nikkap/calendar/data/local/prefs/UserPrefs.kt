package com.nikkap.calendar.data.local.prefs

data class UserPrefs(
    val isAuthorized: Boolean = false,
    val lastSyncTime: Long? = null,
    val email: String? = null,
    val name: String? = null,
    val isFirstLaunch: Boolean = true,
)