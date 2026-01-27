package com.banking.transactions.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class AccountNumberValidator implements ConstraintValidator<ValidAccountNumber, String> {
    
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^ACC-[A-Z0-9]{5}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Allow null values - use @NotNull separately if needed
        if (value == null) {
            return true;
        }
        return ACCOUNT_PATTERN.matcher(value).matches();
    }
}
