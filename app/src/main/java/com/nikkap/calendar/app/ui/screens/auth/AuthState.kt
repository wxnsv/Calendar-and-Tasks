package com.nikkap.calendar.app.ui.screens.auth

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
}