package com.nikkap.calendar.app.ui.screens.auth

import com.google.android.gms.common.api.Scope

data class AuthState(
    val isLoading: Boolean = false,
    val isCalendarGranted: Boolean = false,
    val isTasksGranted: Boolean = false,
    val isProfileGranted: Boolean = false,
    val photoUri: String = "",
    val isAllGranted: Boolean = false,
    val email: String? = null,
    val requiredScopes: List<Scope> = emptyList(),
    val isFirstLaunch: Boolean = true,
    val errorMessage: String? = null
)