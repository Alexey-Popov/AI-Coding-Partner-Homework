package com.banking.api.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validator implementation for {@link ValidTransactionType} annotation.
 * 
 * <p>This validator checks that a transaction type string is one of the
 * three supported types: deposit, withdrawal, or transfer.
 * 
 * <p>The validation is case-insensitive to provide flexibility in input,
 * but the service layer will convert the type to the appropriate enum value.
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
public class TransactionTypeValidator implements ConstraintValidator<ValidTransactionType, String> {
    
    /**
     * Set of valid transaction types (lowercase for case-insensitive comparison).
     */
    private static final Set<String> VALID_TYPES = Set.of(
        "deposit",
        "withdrawal",
        "transfer"
    );
    
    /**
     * Initializes the validator.
     * 
     * @param constraintAnnotation The annotation instance
     */
    @Override
    public void initialize(ValidTransactionType constraintAnnotation) {
        // No initialization needed
    }
    
    /**
     * Validates the transaction type.
     * 
     * <p>Validation Rules:
     * <ul>
     *   <li>Null values are considered invalid (use @NotNull for explicit message)</li>
     *   <li>Must be one of: deposit, withdrawal, transfer (case-insensitive)</li>
     * </ul>
     * 
     * @param type The transaction type to validate
     * @param context The constraint validator context
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValid(String type, ConstraintValidatorContext context) {
        // Null values should be handled by @NotNull annotation
        if (type == null) {
            return false;
        }
        
        // Trim whitespace
        String trimmedType = type.trim();
        
        // Check if empty after trimming
        if (trimmedType.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Transaction type cannot be empty")
                   .addConstraintViolation();
            return false;
        }
        
        // Convert to lowercase for case-insensitive comparison
        String lowerType = trimmedType.toLowerCase();
        
        // Check if type is in the valid set
        if (!VALID_TYPES.contains(lowerType)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Transaction type must be one of: deposit, withdrawal, transfer"
            ).addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
