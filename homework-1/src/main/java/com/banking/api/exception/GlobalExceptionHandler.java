package com.banking.api.exception;

import com.banking.api.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        
        for (ValidationException.ValidationError error : ex.getErrors()) {
            fieldErrors.add(ErrorResponse.FieldError.builder()
                    .field(error.getField())
                    .message(error.getMessage())
                    .build());
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ex.getMessage())
                .details(fieldErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error(ex.getMessage())
                .details(new ArrayList<>())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.add(ErrorResponse.FieldError.builder()
                    .field(error.getField())
                    .message(error.getDefaultMessage())
                    .build());
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("Validation failed")
                .details(fieldErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("An unexpected error occurred: " + ex.getMessage())
                .details(new ArrayList<>())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
