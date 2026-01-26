package com.banking.api.exception;

import com.banking.api.dto.ValidationError;
import lombok.Getter;

import java.util.List;

/**
 * Custom exception for business rule validation failures.
 * 
 * <p>This exception is thrown when business logic validation fails
 * (as opposed to bean validation which throws MethodArgumentNotValidException).
 * 
 * <p><b>Use Cases:</b>
 * <ul>
 *   <li>TRANSFER transactions require both fromAccount and toAccount</li>
 *   <li>TRANSFER transactions require fromAccount != toAccount</li>
 *   <li>DEPOSIT transactions require toAccount</li>
 *   <li>WITHDRAWAL transactions require fromAccount</li>
 * </ul>
 * 
 * <p>This exception carries a list of {@link ValidationError} objects
 * to provide detailed information about what validation rules failed.
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Getter
public class ValidationException extends RuntimeException {
    
    /**
     * List of validation errors describing what went wrong.
     */
    private final List<ValidationError> validationErrors;
    
    /**
     * Creates a validation exception with a list of errors.
     * 
     * @param message General error message
     * @param validationErrors List of specific field validation errors
     */
    public ValidationException(String message, List<ValidationError> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }
    
    /**
     * Creates a validation exception with a single error.
     * 
     * @param message General error message
     * @param field The field that failed validation
     * @param fieldMessage Specific message about the field error
     */
    public ValidationException(String message, String field, String fieldMessage) {
        super(message);
        this.validationErrors = List.of(
            ValidationError.builder()
                .field(field)
                .message(fieldMessage)
                .build()
        );
    }
}
