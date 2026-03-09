package com.example.banking.validation;

import com.example.banking.dto.CreateTransactionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TransactionAccountsValidator implements ConstraintValidator<ValidTransactionAccounts, CreateTransactionRequest> {

    @Override
    public boolean isValid(CreateTransactionRequest request, ConstraintValidatorContext context) {
        // Both fromAccount and toAccount are now required via @NotBlank validation
        // This validator can be used for additional business rules if needed
        return true;
    }
}
