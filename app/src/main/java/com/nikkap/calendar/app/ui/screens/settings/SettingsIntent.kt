package com.nikkap.calendar.app.ui.screens.settings

sealed interface SettingsIntent {
    data class UpdateTheme(val isLight: Boolean?) : SettingsIntent
    data class UpdateStartScreen(val isList: Boolean?) : SettingsIntent
    data class UpdateFirstDayOfWeek(val isMonday: Boolean?) : SettingsIntent
    data class UpdateIsBlankPhoto(val isBlank: Boolean) : SettingsIntent
}