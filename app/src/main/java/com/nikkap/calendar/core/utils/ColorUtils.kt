package com.nikkap.calendar.core.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

enum class CalendarColors(val id: Int, val hex: String) {
    LAVENDER(1, "#a4bdfc"), SAGE(2, "#7ae7bf"),
    GRAPE(3, "#dbadff"), FLAMINGO(4, "#ff887c"),
    BANANA(5, "#fbd75b"), TANGERINE(6, "#ffb878"),
    PEACOCK(7, "#039be5"), GRAPHITE(8, "#e1e1e1"),
    BLUEBERRY(9, "#5484ed"), BASIL(10, "#51b749"),
    TOMATO(11, "#dc2127");

    companion object {
        fun getEventColor(id: Int?) = CalendarColors.entries.find { it.id == id } ?: PEACOCK

        fun getTaskColor() = "#3F51B5"

        fun getBirthdayColor(id: Int?) = CalendarColors.entries.find { it.id == id } ?: SAGE
    }
}

fun String?.toColor(): Color {
    if (this.isNullOrEmpty()) return Color.Gray

    return try {
        val hexToParse = if (this.startsWith("#")) this else "#$this"

        Color(hexToParse.toColorInt())
    } catch (_: Exception) {
        Color.Black
    }
}