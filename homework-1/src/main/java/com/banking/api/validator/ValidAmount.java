package com.banking.api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for transaction amounts.
 * 
 * <p>This annotation ensures that transaction amounts meet the following criteria:
 * <ul>
 *   <li>Must be a positive number (greater than zero)</li>
 *   <li>Must have a maximum of 2 decimal places</li>
 * </ul>
 * 
 * <p><b>Valid Examples:</b>
 * <ul>
 *   <li>100.00</li>
 *   <li>0.01</li>
 *   <li>1234567.89</li>
 * </ul>
 * 
 * <p><b>Invalid Examples:</b>
 * <ul>
 *   <li>0.00 (not positive)</li>
 *   <li>-50.00 (negative)</li>
 *   <li>100.123 (more than 2 decimal places)</li>
 * </ul>
 * 
 * <p><b>Usage:</b>
 * <pre>
 * public class TransactionRequest {
 *     &#64;ValidAmount
 *     private BigDecimal amount;
 * }
 * </pre>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AmountValidator.class)
@Documented
public @interface ValidAmount {
    
    /**
     * The error message to return when validation fails.
     * 
     * @return The validation error message
     */
    String message() default "Amount must be a positive number with maximum 2 decimal places";
    
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
