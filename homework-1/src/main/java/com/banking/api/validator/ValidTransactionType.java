package com.banking.api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for transaction types.
 * 
 * <p>This annotation validates that a transaction type is one of the
 * three supported types: deposit, withdrawal, or transfer.
 * 
 * <p><b>Valid Transaction Types:</b>
 * <ul>
 *   <li>deposit - Money added to an account</li>
 *   <li>withdrawal - Money removed from an account</li>
 *   <li>transfer - Money moved between two accounts</li>
 * </ul>
 * 
 * <p>The validation is case-insensitive, so "DEPOSIT", "Deposit", and "deposit"
 * are all valid. However, the API typically expects lowercase values.
 * 
 * <p><b>Valid Examples:</b>
 * <ul>
 *   <li>deposit</li>
 *   <li>withdrawal</li>
 *   <li>transfer</li>
 *   <li>DEPOSIT (case-insensitive)</li>
 * </ul>
 * 
 * <p><b>Invalid Examples:</b>
 * <ul>
 *   <li>payment</li>
 *   <li>refund</li>
 *   <li>debit</li>
 *   <li>credit</li>
 * </ul>
 * 
 * <p><b>Usage:</b>
 * <pre>
 * public class TransactionRequest {
 *     &#64;ValidTransactionType
 *     private String type;
 * }
 * </pre>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TransactionTypeValidator.class)
@Documented
public @interface ValidTransactionType {
    
    /**
     * The error message to return when validation fails.
     * 
     * @return The validation error message
     */
    String message() default "Transaction type must be one of: deposit, withdrawal, transfer";
    
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
