package com.banking.api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for account numbers.
 * 
 * <p>This annotation validates that account numbers follow the required format:
 * <b>ACC-XXXXX</b> where X is an alphanumeric character (A-Z, 0-9).
 * 
 * <p><b>Format Requirements:</b>
 * <ul>
 *   <li>Starts with "ACC-" prefix (uppercase)</li>
 *   <li>Followed by exactly 5 alphanumeric characters (uppercase letters or digits)</li>
 *   <li>Total length: 9 characters</li>
 * </ul>
 * 
 * <p><b>Valid Examples:</b>
 * <ul>
 *   <li>ACC-12345</li>
 *   <li>ACC-ABCDE</li>
 *   <li>ACC-A1B2C</li>
 * </ul>
 * 
 * <p><b>Invalid Examples:</b>
 * <ul>
 *   <li>ACC-1234 (too short)</li>
 *   <li>ACC-123456 (too long)</li>
 *   <li>acc-12345 (lowercase prefix)</li>
 *   <li>ACC-1234a (lowercase letter)</li>
 *   <li>12345 (missing prefix)</li>
 * </ul>
 * 
 * <p><b>Usage:</b>
 * <pre>
 * public class TransactionRequest {
 *     &#64;ValidAccountNumber
 *     private String fromAccount;
 * }
 * </pre>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AccountNumberValidator.class)
@Documented
public @interface ValidAccountNumber {
    
    /**
     * The error message to return when validation fails.
     * 
     * @return The validation error message
     */
    String message() default "Account number must follow format ACC-XXXXX (5 alphanumeric characters)";
    
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
