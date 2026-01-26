package com.banking.transactions.exception;

import com.banking.transactions.dto.ValidationErrorResponse;

public class ValidationException extends RuntimeException {
    
    private final ValidationErrorResponse errorResponse;

    public ValidationException(ValidationErrorResponse errorResponse) {
        super("Validation failed");
        this.errorResponse = errorResponse;
    }

    public ValidationErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
