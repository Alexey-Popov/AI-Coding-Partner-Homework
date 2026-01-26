package com.banking.api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for currency codes.
 * 
 * <p>This annotation validates that a currency code is a valid ISO 4217
 * three-letter currency code. Only a predefined set of commonly used
 * currencies are accepted.
 * 
 * <p><b>Supported Currency Codes:</b>
 * <ul>
 *   <li>USD - US Dollar</li>
 *   <li>EUR - Euro</li>
 *   <li>GBP - British Pound Sterling</li>
 *   <li>JPY - Japanese Yen</li>
 *   <li>AUD - Australian Dollar</li>
 *   <li>CAD - Canadian Dollar</li>
 *   <li>CHF - Swiss Franc</li>
 *   <li>CNY - Chinese Yuan</li>
 *   <li>SEK - Swedish Krona</li>
 *   <li>NZD - New Zealand Dollar</li>
 *   <li>INR - Indian Rupee</li>
 *   <li>BRL - Brazilian Real</li>
 * </ul>
 * 
 * <p><b>Valid Examples:</b>
 * <ul>
 *   <li>USD</li>
 *   <li>EUR</li>
 *   <li>GBP</li>
 * </ul>
 * 
 * <p><b>Invalid Examples:</b>
 * <ul>
 *   <li>usd (lowercase)</li>
 *   <li>XXX (not in supported list)</li>
 *   <li>US (too short)</li>
 *   <li>DOLLAR (too long)</li>
 * </ul>
 * 
 * <p><b>Usage:</b>
 * <pre>
 * public class TransactionRequest {
 *     &#64;ValidCurrency
 *     private String currency;
 * }
 * </pre>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyCodeValidator.class)
@Documented
public @interface ValidCurrency {
    
    /**
     * The error message to return when validation fails.
     * 
     * @return The validation error message
     */
    String message() default "Currency code must be a valid ISO 4217 code (USD, EUR, GBP, JPY, etc.)";
    
    /**
     * Allows specification of validation groups.
     * 
     * @return The validation groups
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload for clients to assign custom payload objects to a constraint.
     * 
     * @return The payload
     */
    Class<? extends Payload>[] payload() default {};
}
