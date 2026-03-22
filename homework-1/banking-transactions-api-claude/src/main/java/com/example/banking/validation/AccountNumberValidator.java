package com.example.banking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class AccountNumberValidator implements ConstraintValidator<ValidAccountNumber, String> {

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^ACC-[A-Za-z0-9]{5}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/empty validation if needed
        }

        return ACCOUNT_PATTERN.matcher(value).matches();
    }
}
