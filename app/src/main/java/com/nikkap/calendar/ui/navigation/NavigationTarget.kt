package com.nikkap.calendar.ui.navigation


sealed class NavigationTarget {

    object Auth : NavigationTarget()

    object Pager : NavigationTarget()

    data class Create(val type: String, val itemId: String? = null) : NavigationTarget()
}