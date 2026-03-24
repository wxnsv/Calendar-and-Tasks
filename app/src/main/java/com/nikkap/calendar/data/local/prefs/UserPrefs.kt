package com.nikkap.calendar.data.local.prefs

data class UserPrefs(
    val isAuthorized: Boolean = false,
    val eventLastSync: Long? = null,
    val birthdayLastSync: Long? = null,
    val taskLastSync: Long? = null,
    val email: String? = null,
    val name: String? = null,
    val isFirstLaunch: Boolean = true,
)