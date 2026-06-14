package com.nikkap.calendar.app.ui.screens.settings

data class SettingsState(
    val userName: String = "",
    val userImagePath: String? = null,
    val userEmail: String = "",
    val isMondayFirstDayOfWeek: Boolean? = null,
    val isLightTheme: Boolean? = null,
    val isListStartScreen: Boolean? = null
)
