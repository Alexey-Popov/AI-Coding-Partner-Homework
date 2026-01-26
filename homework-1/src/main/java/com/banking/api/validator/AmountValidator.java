package com.banking.api.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Validator implementation for {@link ValidAmount} annotation.
 * 
 * <p>This validator checks that a BigDecimal amount:
 * <ul>
 *   <li>Is not null</li>
 *   <li>Is greater than zero (positive)</li>
 *   <li>Has a maximum of 2 decimal places</li>
 * </ul>
 * 
 * <p>The decimal place check is performed by comparing the scale of the
 * BigDecimal value. A scale greater than 2 indicates more than 2 decimal places.
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
public class AmountValidator implements ConstraintValidator<ValidAmount, BigDecimal> {
    
    private static final int MAX_DECIMAL_PLACES = 2;
    
    /**
     * Initializes the validator.
     * 
     * @param constraintAnnotation The annotation instance
     */
    @Override
    public void initialize(ValidAmount constraintAnnotation) {
        // No initialization needed
    }
    
    /**
     * Validates the amount value.
     * 
     * <p><b>Validation Rules:</b>
     * <ol>
     *   <li>Null values are considered invalid (use @NotNull for explicit message)</li>
     *   <li>Amount must be greater than zero</li>
     *   <li>Amount must have at most 2 decimal places</li>
     * </ol>
     * 
     * @param amount The amount to validate
     * @param context The constraint validator context
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValid(BigDecimal amount, ConstraintValidatorContext context) {
        // Null values should be handled by @NotNull annotation
        if (amount == null) {
            return false;
        }
        
        // Check if amount is positive (greater than zero)
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Amount must be greater than zero")
                   .addConstraintViolation();
            return false;
        }
        
        // Check decimal places
        // Scale returns the number of digits to the right of the decimal point
        if (amount.scale() > MAX_DECIMAL_PLACES) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Amount must have maximum 2 decimal places")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
