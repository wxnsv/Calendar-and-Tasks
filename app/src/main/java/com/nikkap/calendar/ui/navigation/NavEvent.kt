package com.nikkap.calendar.ui.navigation


sealed class NavEvent {
    data class NavigateTo(val route: NavigationTarget) : NavEvent()

    data class SetRoot(val route: NavigationTarget) : NavEvent()
    object PopBack : NavEvent()
}
