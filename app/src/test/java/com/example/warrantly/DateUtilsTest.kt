package com.example.warrantly

import com.example.warrantly.util.formatDate
import com.example.warrantly.util.formatPrice
import com.example.warrantly.util.toEpochMillis
import com.example.warrantly.util.toLocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun testLocalDateToEpochAndBack() {
        val original = LocalDate.of(2025, 6, 15)
        val epoch = original.toEpochMillis()
        val converted = epoch.toLocalDate()
        assertEquals(original, converted)
    }

    @Test
    fun testFormatDate() {
        val date = LocalDate.of(2025, 1, 20).toEpochMillis()
        val formatted = date.formatDate()
        assertTrue(formatted.contains("Jan") && formatted.contains("20") && formatted.contains("2025"))
    }

    @Test
    fun testFormatPriceUsd() {
        val formatted = formatPrice(1999L, "USD")
        assertTrue(formatted.contains("19.99"))
    }

    @Test
    fun testFormatPriceNullCentsReturnsEmpty() {
        val formatted = formatPrice(null, "USD")
        assertEquals("", formatted)
    }

    @Test
    fun testFormatPriceZeroCents() {
        val formatted = formatPrice(0L, "USD")
        assertTrue(formatted.contains("0.00"))
    }
}

