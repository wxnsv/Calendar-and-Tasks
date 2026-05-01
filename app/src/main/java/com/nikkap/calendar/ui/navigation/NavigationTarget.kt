package com.nikkap.calendar.ui.navigation


sealed class NavigationTarget {

    object List : NavigationTarget()

    object Auth : NavigationTarget()

    object Split : NavigationTarget()

    data class Create(val type: String, val itemId: String? = null) : NavigationTarget()
}