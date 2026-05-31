package com.nikkap.calendar.ui.navigation


sealed class NavigationTarget {

    object Auth : NavigationTarget()

    object Pager : NavigationTarget()

    object Settings : NavigationTarget()

    object About : NavigationTarget()

    data class Create(val type: String, val itemId: String = "") : NavigationTarget()
}