package com.example.warrantly.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

private val SHORT_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

private val MEDIUM_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())

/**
 * Converts epoch milliseconds (UTC) to a [LocalDate].
 */
fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

/**
 * Converts a [LocalDate] to epoch milliseconds at the start of day (UTC).
 */
fun LocalDate.toEpochMillis(): Long =
    this.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/**
 * Formats epoch milliseconds as "MMM d, yyyy" (e.g. "Jan 5, 2024").
 */
fun Long.formatDate(): String =
    this.toLocalDate().format(SHORT_DATE_FORMATTER)

/**
 * Formats epoch milliseconds as "MMMM d, yyyy" (e.g. "January 5, 2024").
 */
fun Long.formatDateMedium(): String =
    this.toLocalDate().format(MEDIUM_DATE_FORMATTER)

/**
 * Formats a price stored in minor currency units (cents) as a locale-aware string.
 * Returns an empty string if [cents] is null.
 * Examples: "$12.99", "£5.00", "€9.49"
 */
fun formatPrice(cents: Long?, currencyCode: String): String {
    if (cents == null) return ""
    return try {
        val currency = Currency.getInstance(currencyCode)
        val symbol = currency.symbol
        val fractionDigits = currency.defaultFractionDigits
        val major = cents / 100.0
        val formatted = "%.${fractionDigits}f".format(major)
        "$symbol$formatted"
    } catch (e: IllegalArgumentException) {
        // Fallback: unknown currency code — show raw decimal
        val major = cents / 100.0
        "%.2f $currencyCode".format(major)
    }
}
