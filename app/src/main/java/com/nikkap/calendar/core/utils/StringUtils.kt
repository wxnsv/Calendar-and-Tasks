package com.nikkap.calendar.core.utils

/**
 * Removes the system suffix added by Google Event to birthday events
 * (e.g., "John Doe – Birthday" becomes "John Doe").
 * * It identifies the suffix by finding the last occurrence of a separator
 * (dash or colon) surrounded by whitespace, which is a common pattern
 * across different localized Google Event languages.
 */
fun String.trimBirthdaySuffix(): String {

    val regex = Regex("""\s+[–—:-]\s+""")
    val matches = regex.findAll(this).toList()

    return if (matches.isNotEmpty()) {
        val lastMatch = matches.last()
        this.substring(0, lastMatch.range.first).trim()
    } else {
        this
    }
}