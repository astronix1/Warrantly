package com.example.warrantly.domain.usecase

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

object ProductFormValidator {

    private const val MAX_NAME_LENGTH = 200
    private const val MAX_PRICE_VALUE = 999_999_99L // $999,999.99 in cents

    /**
     * Validates the product name.
     * Returns an error message string, or null if valid.
     */
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Product name is required"
            name.trim().length < 2 -> "Product name must be at least 2 characters"
            name.length > MAX_NAME_LENGTH -> "Product name must be less than $MAX_NAME_LENGTH characters"
            else -> null
        }
    }

    /**
     * Validates the purchase date.
     * Returns an error message string, or null if valid.
     */
    fun validatePurchaseDate(date: Long?): String? {
        if (date == null) return "Purchase date is required"

        val purchaseLocalDate = Instant.ofEpochMilli(date)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        val today = LocalDate.now()

        return when {
            purchaseLocalDate.isAfter(today) -> "Purchase date cannot be in the future"
            else -> null
        }
    }

    /**
     * Validates warranty start and end dates in relation to each other and the purchase date.
     * Returns an error message string, or null if valid.
     */
    fun validateWarrantyDates(startDate: Long?, endDate: Long?, purchaseDate: Long?): String? {
        if (startDate == null && endDate == null) return null

        if (endDate != null && startDate != null) {
            val start = Instant.ofEpochMilli(startDate).atZone(ZoneOffset.UTC).toLocalDate()
            val end = Instant.ofEpochMilli(endDate).atZone(ZoneOffset.UTC).toLocalDate()
            if (!end.isAfter(start)) {
                return "Warranty end date must be after the start date"
            }
        }

        if (endDate != null && purchaseDate != null) {
            val purchase = Instant.ofEpochMilli(purchaseDate).atZone(ZoneOffset.UTC).toLocalDate()
            val end = Instant.ofEpochMilli(endDate).atZone(ZoneOffset.UTC).toLocalDate()
            if (end.isBefore(purchase)) {
                return "Warranty end date cannot be before the purchase date"
            }
        }

        if (startDate != null && purchaseDate != null) {
            val purchase = Instant.ofEpochMilli(purchaseDate).atZone(ZoneOffset.UTC).toLocalDate()
            val start = Instant.ofEpochMilli(startDate).atZone(ZoneOffset.UTC).toLocalDate()
            if (start.isBefore(purchase)) {
                return "Warranty start date cannot be before the purchase date"
            }
        }

        return null
    }

    /**
     * Validates and parses a price string.
     * Returns a [Pair] of (price in cents as Long?, error message as String?).
     * If the price text is blank/empty, returns (null, null) — price is optional.
     */
    fun validatePrice(priceText: String): Pair<Long?, String?> {
        val trimmed = priceText.trim()
        if (trimmed.isEmpty()) return Pair(null, null)

        // Strip common currency symbols before parsing
        val sanitized = trimmed.replace(Regex("[\\$£€¥₹,\\s]"), "")

        val price = sanitized.toDoubleOrNull()
            ?: return Pair(null, "Invalid price format")

        if (price < 0) return Pair(null, "Price cannot be negative")

        val cents = (price * 100).toLong()

        if (cents > MAX_PRICE_VALUE) {
            return Pair(null, "Price is too large")
        }

        return Pair(cents, null)
    }
}
