package com.nikkap.calendar.ui.screens.main

import com.nikkap.calendar.data.local.prefs.UserPrefs

data class MainState(
    val userState: UserPrefs = UserPrefs(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)
