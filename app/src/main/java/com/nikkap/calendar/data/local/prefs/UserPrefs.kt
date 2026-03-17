package com.nikkap.calendar.data.local.prefs

data class UserPrefs(
    val isAuthorized: Boolean,
    val lastSyncTime: Long?,
    val email: String?,
    val name: String?
)