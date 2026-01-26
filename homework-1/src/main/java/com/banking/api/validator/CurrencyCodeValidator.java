package com.banking.api.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validator implementation for {@link ValidCurrency} annotation.
 * 
 * <p>This validator checks that a currency code is in the set of
 * supported ISO 4217 currency codes. The validation is case-insensitive
 * but expects uppercase input for consistency.
 * 
 * <p>The supported currencies are defined in the application configuration
 * but hardcoded here for validation purposes. This ensures fast validation
 * without external dependencies.
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
public class CurrencyCodeValidator implements ConstraintValidator<ValidCurrency, String> {
    
    /**
     * Set of valid ISO 4217 currency codes.
     * This matches the configuration in application.properties.
     */
    private static final Set<String> VALID_CURRENCIES = Set.of(
        "USD",  // US Dollar
        "EUR",  // Euro
        "GBP",  // British Pound Sterling
        "JPY",  // Japanese Yen
        "AUD",  // Australian Dollar
        "CAD",  // Canadian Dollar
        "CHF",  // Swiss Franc
        "CNY",  // Chinese Yuan
        "SEK",  // Swedish Krona
        "NZD",  // New Zealand Dollar
        "INR",  // Indian Rupee
        "BRL"   // Brazilian Real
    );
    
    /**
     * Initializes the validator.
     * 
     * @param constraintAnnotation The annotation instance
     */
    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        // No initialization needed
    }
    
    /**
     * Validates the currency code.
     * 
     * <p>Validation Rules:
     * <ul>
     *   <li>Null values are considered invalid (use @NotNull for explicit message)</li>
     *   <li>Must be exactly 3 characters long</li>
     *   <li>Must be in the set of valid currency codes (case-insensitive check)</li>
     * </ul>
     * 
     * @param currency The currency code to validate
     * @param context The constraint validator context
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        // Null values should be handled by @NotNull annotation
        if (currency == null) {
            return false;
        }
        
        // Trim whitespace
        String trimmedCurrency = currency.trim();
        
        // Check if empty after trimming
        if (trimmedCurrency.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Currency code cannot be empty")
                   .addConstraintViolation();
            return false;
        }
        
        // Check length (ISO 4217 codes are exactly 3 characters)
        if (trimmedCurrency.length() != 3) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Currency code must be exactly 3 characters")
                   .addConstraintViolation();
            return false;
        }
        
        // Convert to uppercase for case-insensitive comparison
        String upperCurrency = trimmedCurrency.toUpperCase();
        
        // Check if currency is in the valid set
        if (!VALID_CURRENCIES.contains(upperCurrency)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Invalid currency code. Supported currencies: " + String.join(", ", VALID_CURRENCIES)
            ).addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
