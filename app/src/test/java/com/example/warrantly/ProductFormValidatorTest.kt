package com.example.warrantly

import com.example.warrantly.domain.usecase.ProductFormValidator
import com.example.warrantly.util.toEpochMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ProductFormValidatorTest {

    @Test
    fun testEmptyNameIsInvalid() {
        val err = ProductFormValidator.validateName("")
        assertNotNull(err)
        assertEquals("Product name is required", err)
    }

    @Test
    fun testBlankNameIsInvalid() {
        val err = ProductFormValidator.validateName("   ")
        assertNotNull(err)
    }

    @Test
    fun testShortNameIsInvalid() {
        val err = ProductFormValidator.validateName("A")
        assertNotNull(err)
    }

    @Test
    fun testValidName() {
        val err = ProductFormValidator.validateName("MacBook Pro 16")
        assertNull(err)
    }

    @Test
    fun testNullPurchaseDateIsInvalid() {
        val err = ProductFormValidator.validatePurchaseDate(null)
        assertNotNull(err)
    }

    @Test
    fun testFuturePurchaseDateIsInvalid() {
        val futureDate = LocalDate.now().plusDays(5).toEpochMillis()
        val err = ProductFormValidator.validatePurchaseDate(futureDate)
        assertNotNull(err)
    }

    @Test
    fun testPastPurchaseDateIsValid() {
        val pastDate = LocalDate.now().minusMonths(6).toEpochMillis()
        val err = ProductFormValidator.validatePurchaseDate(pastDate)
        assertNull(err)
    }

    @Test
    fun testWarrantyEndBeforeStartIsInvalid() {
        val purchase = LocalDate.now().minusMonths(1).toEpochMillis()
        val start = LocalDate.now().toEpochMillis()
        val end = LocalDate.now().minusDays(10).toEpochMillis()

        val err = ProductFormValidator.validateWarrantyDates(start, end, purchase)
        assertNotNull(err)
        assertEquals("Warranty end date must be after the start date", err)
    }

    @Test
    fun testWarrantyEndBeforePurchaseIsInvalid() {
        val purchase = LocalDate.now().minusMonths(1).toEpochMillis()
        val end = LocalDate.now().minusMonths(2).toEpochMillis()

        val err = ProductFormValidator.validateWarrantyDates(null, end, purchase)
        assertNotNull(err)
    }

    @Test
    fun testValidWarrantyDates() {
        val purchase = LocalDate.now().minusMonths(2).toEpochMillis()
        val start = LocalDate.now().minusMonths(2).toEpochMillis()
        val end = LocalDate.now().plusMonths(10).toEpochMillis()

        val err = ProductFormValidator.validateWarrantyDates(start, end, purchase)
        assertNull(err)
    }

    @Test
    fun testPriceValidationEmptyReturnsNull() {
        val (cents, err) = ProductFormValidator.validatePrice("")
        assertNull(cents)
        assertNull(err)
    }

    @Test
    fun testPriceValidationValidFormats() {
        val (cents1, err1) = ProductFormValidator.validatePrice("29.99")
        assertEquals(2999L, cents1)
        assertNull(err1)

        val (cents2, err2) = ProductFormValidator.validatePrice("$1,299.50")
        assertEquals(129950L, cents2)
        assertNull(err2)

        val (cents3, err3) = ProductFormValidator.validatePrice("100")
        assertEquals(10000L, cents3)
        assertNull(err3)
    }

    @Test
    fun testPriceValidationInvalidText() {
        val (cents, err) = ProductFormValidator.validatePrice("not-a-number")
        assertNull(cents)
        assertNotNull(err)
    }

    @Test
    fun testNegativePriceIsInvalid() {
        val (cents, err) = ProductFormValidator.validatePrice("-50.00")
        assertNull(cents)
        assertNotNull(err)
    }
}

