package com.banking.api.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class AmountValidator implements ConstraintValidator<ValidAmount, BigDecimal> {
    
    @Override
    public boolean isValid(BigDecimal amount, ConstraintValidatorContext context) {
        if (amount == null) {
            return true;
        }
        
        // Check if positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Amount must be a positive number")
                    .addConstraintViolation();
            return false;
        }
        
        // Check decimal places (max 2)
        if (amount.scale() > 2) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Amount must have maximum 2 decimal places")
                    .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
