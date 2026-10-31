package com.example.warrantly.domain.usecase

import com.example.warrantly.data.model.WarrantyStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

object WarrantyStatusUseCase {

    const val EXPIRING_SOON_THRESHOLD_DAYS = 30

    /**
     * Returns the [WarrantyStatus] for a given [warrantyEndDate] (epoch millis or null).
     */
    fun getStatus(warrantyEndDate: Long?): WarrantyStatus {
        if (warrantyEndDate == null) return WarrantyStatus.UNKNOWN

        val today = LocalDate.now()
        val endDate = Instant.ofEpochMilli(warrantyEndDate)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        return when {
            endDate.isBefore(today) -> WarrantyStatus.EXPIRED
            !endDate.isAfter(today.plusDays(EXPIRING_SOON_THRESHOLD_DAYS.toLong())) -> WarrantyStatus.EXPIRING_SOON
            else -> WarrantyStatus.ACTIVE
        }
    }

    /**
     * Returns a human-readable time-remaining string.
     * e.g. "23 days remaining", "Expired 5 days ago", "Expires today"
     */
    fun getTimeRemainingText(warrantyEndDate: Long?): String {
        if (warrantyEndDate == null) return "No warranty information"

        val today = LocalDate.now()
        val endDate = Instant.ofEpochMilli(warrantyEndDate)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        val daysUntil = ChronoUnit.DAYS.between(today, endDate)

        return when {
            daysUntil == 0L -> "Expires today"
            daysUntil > 0 -> {
                val years = daysUntil / 365
                val months = (daysUntil % 365) / 30
                val days = daysUntil % 30
                when {
                    years > 0 && months > 0 -> "$years year${if (years > 1) "s" else ""} $months month${if (months > 1) "s" else ""} remaining"
                    years > 0 -> "$years year${if (years > 1) "s" else ""} remaining"
                    months > 0 && days > 0 -> "$months month${if (months > 1) "s" else ""} $days day${if (days > 1) "s" else ""} remaining"
                    months > 0 -> "$months month${if (months > 1) "s" else ""} remaining"
                    else -> "$daysUntil day${if (daysUntil > 1) "s" else ""} remaining"
                }
            }
            else -> {
                val daysAgo = -daysUntil
                "Expired ${daysAgo} day${if (daysAgo > 1) "s" else ""} ago"
            }
        }
    }

    /**
     * Returns days until warranty expiry (negative means already expired), or null if no end date.
     */
    fun getDaysUntilExpiry(warrantyEndDate: Long?): Int? {
        if (warrantyEndDate == null) return null

        val today = LocalDate.now()
        val endDate = Instant.ofEpochMilli(warrantyEndDate)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        return ChronoUnit.DAYS.between(today, endDate).toInt()
    }
}
