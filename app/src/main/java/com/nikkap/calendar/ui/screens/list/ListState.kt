package com.nikkap.calendar.ui.screens.list

data class ListState(
    val items: List<ListItem> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val isMenuExpanded: Boolean = false
)