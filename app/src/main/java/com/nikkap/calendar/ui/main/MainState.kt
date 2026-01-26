package com.nikkap.calendar.ui.main

data class MainState(
    var items: List<ListItem> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)