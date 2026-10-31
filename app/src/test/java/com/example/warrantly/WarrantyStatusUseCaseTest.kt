package com.example.warrantly

import com.example.warrantly.data.model.WarrantyStatus
import com.example.warrantly.domain.usecase.WarrantyStatusUseCase
import com.example.warrantly.util.toEpochMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WarrantyStatusUseCaseTest {

    @Test
    fun testNullWarrantyEndDateReturnsUnknown() {
        val status = WarrantyStatusUseCase.getStatus(null)
        assertEquals(WarrantyStatus.UNKNOWN, status)

        val days = WarrantyStatusUseCase.getDaysUntilExpiry(null)
        assertNull(days)

        val text = WarrantyStatusUseCase.getTimeRemainingText(null)
        assertEquals("No warranty information", text)
    }

    @Test
    fun testExpiryYesterdayReturnsExpired() {
        val yesterday = LocalDate.now().minusDays(1).toEpochMillis()
        val status = WarrantyStatusUseCase.getStatus(yesterday)
        assertEquals(WarrantyStatus.EXPIRED, status)

        val days = WarrantyStatusUseCase.getDaysUntilExpiry(yesterday)
        assertEquals(-1, days)

        val text = WarrantyStatusUseCase.getTimeRemainingText(yesterday)
        assertTrue(text.contains("Expired") || text.contains("ago"))
    }

    @Test
    fun testExpiryTodayReturnsExpiringSoon() {
        // Today is not expired yet — user still has coverage today!
        val today = LocalDate.now().toEpochMillis()
        val status = WarrantyStatusUseCase.getStatus(today)
        assertEquals(WarrantyStatus.EXPIRING_SOON, status)

        val days = WarrantyStatusUseCase.getDaysUntilExpiry(today)
        assertEquals(0, days)

        val text = WarrantyStatusUseCase.getTimeRemainingText(today)
        assertEquals("Expires today", text)
    }

    @Test
    fun testExpiryTomorrowReturnsExpiringSoon() {
        val tomorrow = LocalDate.now().plusDays(1).toEpochMillis()
        val status = WarrantyStatusUseCase.getStatus(tomorrow)
        assertEquals(WarrantyStatus.EXPIRING_SOON, status)

        val days = WarrantyStatusUseCase.getDaysUntilExpiry(tomorrow)
        assertEquals(1, days)

        val text = WarrantyStatusUseCase.getTimeRemainingText(tomorrow)
        assertTrue(text.contains("1 day remaining"))
    }

    @Test
    fun testExpiryIn30DaysReturnsExpiringSoon() {
        val in30Days = LocalDate.now().plusDays(30).toEpochMillis()
        val status = WarrantyStatusUseCase.getStatus(in30Days)
        assertEquals(WarrantyStatus.EXPIRING_SOON, status)

        val days = WarrantyStatusUseCase.getDaysUntilExpiry(in30Days)
        assertEquals(30, days)
    }

    @Test
    fun testExpiryIn31DaysReturnsActive() {
        val in31Days = LocalDate.now().plusDays(31).toEpochMillis()
        val status = WarrantyStatusUseCase.getStatus(in31Days)
        assertEquals(WarrantyStatus.ACTIVE, status)

        val days = WarrantyStatusUseCase.getDaysUntilExpiry(in31Days)
        assertEquals(31, days)
    }

    @Test
    fun testExpiryInOneYearReturnsActive() {
        val inOneYear = LocalDate.now().plusYears(1).toEpochMillis()
        val status = WarrantyStatusUseCase.getStatus(inOneYear)
        assertEquals(WarrantyStatus.ACTIVE, status)

        val text = WarrantyStatusUseCase.getTimeRemainingText(inOneYear)
        assertTrue(text.contains("remaining"))
    }

    @Test
    fun testLeapYearDateHandling() {
        // Feb 29 on leap year
        val leapDate = LocalDate.of(2028, 2, 29).toEpochMillis()
        val status = WarrantyStatusUseCase.getStatus(leapDate)
        // Ensure calculation succeeds without exception
        assertTrue(status == WarrantyStatus.ACTIVE || status == WarrantyStatus.EXPIRED || status == WarrantyStatus.EXPIRING_SOON)
    }
}

