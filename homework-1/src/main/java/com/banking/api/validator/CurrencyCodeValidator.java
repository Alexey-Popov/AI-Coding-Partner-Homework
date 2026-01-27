package com.banking.api.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Currency;
import java.util.Set;

public class CurrencyCodeValidator implements ConstraintValidator<ValidCurrency, String> {
    
    private static final Set<String> VALID_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "CNY", "INR", "MXN"
    );
    
    @Override
    public boolean isValid(String currencyCode, ConstraintValidatorContext context) {
        if (currencyCode == null || currencyCode.isEmpty()) {
            return true;
        }
        
        try {
            Currency.getInstance(currencyCode);
            return VALID_CURRENCIES.contains(currencyCode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
