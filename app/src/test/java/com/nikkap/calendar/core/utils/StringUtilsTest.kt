package com.nikkap.calendar.core.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringUtilsTest {

    /**
     * Tests for [trimBirthdaySuffix]
     */

    @Test
    fun `string trimBirthdaySuffix should return string without Server created suffix`() {
        val actual = "John Doe – Birthday".trimBirthdaySuffix()
        assertEquals("John Doe", actual)
    }

    @Test
    fun `string trimBirthdaySuffix should return string without Server created suffix when string have another suffix`() {
        val actual = "My Project - 1 year - Birthday".trimBirthdaySuffix()
        assertEquals("My Project - 1 year", actual)
    }

    @Test
    fun `string trimBirthdaySuffix should return original string if the string does not contain a suffix`() {
        val actual = "John Doe".trimBirthdaySuffix()
        assertEquals("John Doe", actual)
    }
}