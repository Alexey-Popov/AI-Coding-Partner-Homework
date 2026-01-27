package com.banking.api.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class AccountNumberValidator implements ConstraintValidator<ValidAccountNumber, String> {
    
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^ACC-[A-Z0-9]{5}$");
    
    @Override
    public boolean isValid(String accountNumber, ConstraintValidatorContext context) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            return true; // Use @NotNull for null checks
        }
        return ACCOUNT_PATTERN.matcher(accountNumber).matches();
    }
}
