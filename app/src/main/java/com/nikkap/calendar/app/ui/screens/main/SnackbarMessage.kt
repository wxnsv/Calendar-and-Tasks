package com.nikkap.calendar.app.ui.screens.main

data class SnackbarMessage(
    val message: String,
    val actionText: String,
    val onUndo: () -> Unit
)