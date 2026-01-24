package com.banking.transactions.exception;

import com.banking.transactions.dto.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(ValidationException ex) {
        return new ResponseEntity<>(ex.getErrorResponse(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        
        String message = ex.getMessage();
        if (message != null && message.contains("TransactionType")) {
            errorResponse.addDetail("type", "type must be one of: deposit, withdrawal, transfer");
        } else if (message != null && message.contains("TransactionStatus")) {
            errorResponse.addDetail("status", "status must be one of: pending, completed, failed");
        } else {
            errorResponse.addDetail("request", "Invalid request format or values");
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleGeneralException(Exception ex) {
        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.addDetail("general", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
