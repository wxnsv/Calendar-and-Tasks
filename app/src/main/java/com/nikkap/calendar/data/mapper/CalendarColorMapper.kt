package com.nikkap.calendar.data.mapper

object CalendarColorMapper {
    private val colors = mapOf(
        "1" to "#a4bdfc", "2" to "#7ae7bf", "3" to "#dbadff",
        "4" to "#ff887c", "5" to "#fbd75b", "6" to "#ffb878",
        "7" to "#46d6db", "8" to "#e1e1e1", "9" to "#5484ed",
        "10" to "#51b749", "11" to "#dc182f"
    )

    fun getColor(id: String?): String = colors[id] ?: "#039be5"
    /** blue by default*/
}