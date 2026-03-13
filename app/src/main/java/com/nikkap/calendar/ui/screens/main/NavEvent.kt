package com.nikkap.calendar.ui.screens.main

sealed class NavEvent {
    object Start : NavEvent()
    object ToList : NavEvent()
    object ToAuth : NavEvent()
    data class ToCreate(val type: String, val itemId: String? = null) : NavEvent()
}