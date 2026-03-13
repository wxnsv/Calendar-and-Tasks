package com.nikkap.calendar.core.utils

enum class CalendarColors(val id: String, val hex: String) {
    LAVENDER("1", "#a4bdfc"), SAGE("2", "#7ae7bf"),
    GRAPE("3", "#dbadff"), FLAMINGO("4", "#ff887c"),
    BANANA("5", "#fbd75b"), TANGERINE("6", "#ffb878"),
    PEACOCK("7", "#070707"), GRAPHITE("8", "#e1e1e1"),
    BLUEBERRY("9", "#5484ed"), BASIL("10", "#51b886"),
    TOMATO("11", "#dc2127");

    companion object {
        fun getEventColor(id: String?) = CalendarColors.entries.find { it.id == id } ?: PEACOCK

        fun getTaskColor() = "#3f51b5"

        fun getBirthdayColor(id: String?) = CalendarColors.entries.find { it.id == id } ?: SAGE
    }
}