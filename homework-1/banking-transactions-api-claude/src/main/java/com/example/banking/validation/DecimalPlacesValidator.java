package com.example.banking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class DecimalPlacesValidator implements ConstraintValidator<DecimalPlaces, BigDecimal> {

    private int maxDecimalPlaces;

    @Override
    public void initialize(DecimalPlaces constraintAnnotation) {
        this.maxDecimalPlaces = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        // Get the scale (number of decimal places)
        int scale = value.stripTrailingZeros().scale();

        // If scale is negative, it means the number is an integer with trailing zeros
        // e.g., 1000 has scale of -3, which is valid
        if (scale < 0) {
            return true;
        }

        return scale <= maxDecimalPlaces;
    }
}
