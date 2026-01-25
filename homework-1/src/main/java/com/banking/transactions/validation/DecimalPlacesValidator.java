package com.banking.transactions.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class DecimalPlacesValidator implements ConstraintValidator<ValidDecimalPlaces, BigDecimal> {
    
    private int maxDecimalPlaces;

    @Override
    public void initialize(ValidDecimalPlaces constraintAnnotation) {
        this.maxDecimalPlaces = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        // Get the scale (number of decimal places)
        int scale = value.stripTrailingZeros().scale();
        
        // If scale is negative, it means the number is a multiple of 10^|scale|
        // (e.g., 1000 has scale -3), which is valid
        return scale <= maxDecimalPlaces;
    }
}
