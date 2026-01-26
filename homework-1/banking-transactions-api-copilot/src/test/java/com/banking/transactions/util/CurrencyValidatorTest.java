package com.banking.transactions.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CurrencyValidator.
 * Tests ISO 4217 currency code validation.
 */
class CurrencyValidatorTest {

    @Test
    void testValidCurrencies() {
        // Major currencies
        assertTrue(CurrencyValidator.isValidCurrency("USD"));
        assertTrue(CurrencyValidator.isValidCurrency("EUR"));
        assertTrue(CurrencyValidator.isValidCurrency("GBP"));
        assertTrue(CurrencyValidator.isValidCurrency("JPY"));
        assertTrue(CurrencyValidator.isValidCurrency("CHF"));
        assertTrue(CurrencyValidator.isValidCurrency("CAD"));
        assertTrue(CurrencyValidator.isValidCurrency("AUD"));
    }

    @Test
    void testValidCurrencies_Asian() {
        assertTrue(CurrencyValidator.isValidCurrency("CNY"));
        assertTrue(CurrencyValidator.isValidCurrency("HKD"));
        assertTrue(CurrencyValidator.isValidCurrency("SGD"));
        assertTrue(CurrencyValidator.isValidCurrency("KRW"));
        assertTrue(CurrencyValidator.isValidCurrency("INR"));
        assertTrue(CurrencyValidator.isValidCurrency("THB"));
    }

    @Test
    void testValidCurrencies_European() {
        assertTrue(CurrencyValidator.isValidCurrency("SEK"));
        assertTrue(CurrencyValidator.isValidCurrency("NOK"));
        assertTrue(CurrencyValidator.isValidCurrency("DKK"));
        assertTrue(CurrencyValidator.isValidCurrency("PLN"));
        assertTrue(CurrencyValidator.isValidCurrency("CZK"));
    }

    @Test
    void testInvalidCurrencies() {
        assertFalse(CurrencyValidator.isValidCurrency("XXX"));
        assertFalse(CurrencyValidator.isValidCurrency("ABC"));
        assertFalse(CurrencyValidator.isValidCurrency("ZZZ"));
        assertFalse(CurrencyValidator.isValidCurrency("INVALID"));
    }

    @Test
    void testInvalidCurrencies_Empty() {
        assertFalse(CurrencyValidator.isValidCurrency(""));
        assertFalse(CurrencyValidator.isValidCurrency(null));
    }

    @Test
    void testInvalidCurrencies_WrongCase() {
        // Validator expects uppercase
        assertFalse(CurrencyValidator.isValidCurrency("usd"));
        assertFalse(CurrencyValidator.isValidCurrency("eur"));
        assertFalse(CurrencyValidator.isValidCurrency("Gbp"));
    }

    @Test
    void testInvalidCurrencies_WrongLength() {
        assertFalse(CurrencyValidator.isValidCurrency("US"));
        assertFalse(CurrencyValidator.isValidCurrency("USDD"));
        assertFalse(CurrencyValidator.isValidCurrency("U"));
    }
}
