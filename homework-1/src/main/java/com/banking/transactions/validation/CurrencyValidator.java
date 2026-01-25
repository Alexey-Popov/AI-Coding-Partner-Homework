package com.banking.transactions.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {
    
    // ISO 4217 common currency codes
    private static final Set<String> VALID_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD",
        "SEK", "NOK", "DKK", "PLN", "CZK", "HUF", "RON", "BGN",
        "TRY", "RUB", "CNY", "INR", "BRL", "MXN", "ZAR", "KRW",
        "SGD", "HKD", "THB", "MYR", "IDR", "PHP", "AED", "SAR"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return VALID_CURRENCIES.contains(value.toUpperCase());
    }
}
