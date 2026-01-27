package com.banking.validator;

import com.banking.controller.dto.CreateTransactionRequest;
import com.banking.controller.dto.ValidationErrorResponse.FieldError;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class TransactionValidator {

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^ACC-[A-Za-z0-9]{5}$");

    public List<FieldError> validate(CreateTransactionRequest request) {
        List<FieldError> errors = new ArrayList<>();

        validateAmount(request.getAmount(), errors);
        validateCurrency(request.getCurrency(), errors);
        validateType(request.getType(), errors);
        validateAccounts(request, errors);

        return errors;
    }

    private void validateAmount(BigDecimal amount, List<FieldError> errors) {
        if (amount == null) {
            errors.add(new FieldError("amount", "Amount is required"));
            return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(new FieldError("amount", "Amount must be a positive number"));
        }
        if (amount.scale() > 2) {
            errors.add(new FieldError("amount", "Amount must have maximum 2 decimal places"));
        }
    }

    private void validateCurrency(String currency, List<FieldError> errors) {
        if (currency == null || currency.isBlank()) {
            errors.add(new FieldError("currency", "Currency is required"));
            return;
        }
        try {
            Currency.getInstance(currency.toUpperCase());
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError("currency", "Invalid currency code"));
        }
    }

    private void validateType(String type, List<FieldError> errors) {
        if (type == null || type.isBlank()) {
            errors.add(new FieldError("type", "Transaction type is required"));
            return;
        }
        if (!type.equals("deposit") && !type.equals("withdrawal") && !type.equals("transfer")) {
            errors.add(new FieldError("type", "Type must be deposit, withdrawal, or transfer"));
        }
    }

    private void validateAccounts(CreateTransactionRequest request, List<FieldError> errors) {
        String type = request.getType();
        if (type == null) return;

        switch (type) {
            case "deposit" -> validateDepositAccounts(request, errors);
            case "withdrawal" -> validateWithdrawalAccounts(request, errors);
            case "transfer" -> validateTransferAccounts(request, errors);
        }
    }

    private void validateDepositAccounts(CreateTransactionRequest request, List<FieldError> errors) {
        if (request.getToAccount() == null || request.getToAccount().isBlank()) {
            errors.add(new FieldError("toAccount", "toAccount is required for deposits"));
        } else if (!ACCOUNT_PATTERN.matcher(request.getToAccount()).matches()) {
            errors.add(new FieldError("toAccount", "Account must follow format ACC-XXXXX"));
        }
    }

    private void validateWithdrawalAccounts(CreateTransactionRequest request, List<FieldError> errors) {
        if (request.getFromAccount() == null || request.getFromAccount().isBlank()) {
            errors.add(new FieldError("fromAccount", "fromAccount is required for withdrawals"));
        } else if (!ACCOUNT_PATTERN.matcher(request.getFromAccount()).matches()) {
            errors.add(new FieldError("fromAccount", "Account must follow format ACC-XXXXX"));
        }
    }

    private void validateTransferAccounts(CreateTransactionRequest request, List<FieldError> errors) {
        if (request.getFromAccount() == null || request.getFromAccount().isBlank()) {
            errors.add(new FieldError("fromAccount", "fromAccount is required for transfers"));
        } else if (!ACCOUNT_PATTERN.matcher(request.getFromAccount()).matches()) {
            errors.add(new FieldError("fromAccount", "Account must follow format ACC-XXXXX"));
        }

        if (request.getToAccount() == null || request.getToAccount().isBlank()) {
            errors.add(new FieldError("toAccount", "toAccount is required for transfers"));
        } else if (!ACCOUNT_PATTERN.matcher(request.getToAccount()).matches()) {
            errors.add(new FieldError("toAccount", "Account must follow format ACC-XXXXX"));
        }
    }
}
