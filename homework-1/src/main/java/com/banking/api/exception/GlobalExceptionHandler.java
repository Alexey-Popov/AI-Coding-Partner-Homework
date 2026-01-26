package com.banking.api.exception;

import com.banking.api.dto.ErrorResponse;
import com.banking.api.dto.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for the Banking Transactions API.
 * 
 * <p>This class intercepts exceptions thrown by controllers and service layers,
 * converting them into consistent error responses with appropriate HTTP status codes.
 * 
 * <p><b>Handled Exception Types:</b>
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} - Returns 400 Bad Request with validation details</li>
 *   <li>{@link ResourceNotFoundException} - Returns 404 Not Found</li>
 *   <li>{@link IllegalArgumentException} - Returns 400 Bad Request</li>
 *   <li>{@link Exception} - Returns 500 Internal Server Error for unexpected errors</li>
 * </ul>
 * 
 * <p>All error responses follow the {@link ErrorResponse} format for consistency.
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles validation errors from {@code @Valid} annotations.
     * 
     * <p>This handler is triggered when request body validation fails,
     * typically from Bean Validation annotations like {@code @NotNull},
     * {@code @Min}, {@code @Max}, {@code @Pattern}, etc.
     * 
     * <p><b>Response Status:</b> 400 Bad Request
     * 
     * <p><b>Example Response:</b>
     * <pre>
     * {
     *   "error": "Validation failed",
     *   "timestamp": "2026-01-22T10:30:00",
     *   "path": "/api/v1/transactions",
     *   "details": [
     *     {
     *       "field": "amount",
     *       "message": "must be greater than 0"
     *     }
     *   ]
     * }
     * </pre>
     * 
     * @param ex The validation exception containing field errors
     * @param request The HTTP request that caused the error
     * @return ResponseEntity with error details and HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        log.warn("Validation failed for request: {}", request.getRequestURI());
        
        List<ValidationError> validationErrors = new ArrayList<>();
        
        // Extract field errors from the exception
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            ValidationError error = ValidationError.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .build();
            validationErrors.add(error);
            
            log.debug("Validation error - Field: {}, Message: {}", 
                    fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
                "Validation failed",
                request.getRequestURI(),
                validationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles custom validation exceptions for business rule violations.
     * 
     * <p>This handler is triggered when business logic validation fails,
     * such as:
     * <ul>
     *   <li>TRANSFER transactions missing required accounts</li>
     *   <li>TRANSFER transactions with same from/to accounts</li>
     *   <li>DEPOSIT transactions missing toAccount</li>
     *   <li>WITHDRAWAL transactions missing fromAccount</li>
     * </ul>
     * 
     * <p><b>Response Status:</b> 400 Bad Request
     * 
     * <p><b>Example Response:</b>
     * <pre>
     * {
     *   "error": "Business rule validation failed",
     *   "timestamp": "2026-01-22T10:30:00",
     *   "path": "/api/v1/transactions",
     *   "details": [
     *     {
     *       "field": "fromAccount",
     *       "message": "From account is required for transfer transactions"
     *     }
     *   ]
     * }
     * </pre>
     * 
     * @param ex The validation exception containing business rule errors
     * @param request The HTTP request that caused the error
     * @return ResponseEntity with error details and HTTP 400 status
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {
        
        log.warn("Business rule validation failed for request: {}", request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getMessage(),
                request.getRequestURI(),
                ex.getValidationErrors()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles resource not found exceptions.
     * 
     * <p>This handler is triggered when a requested resource (e.g., transaction, account)
     * cannot be found in the system.
     * 
     * <p><b>Response Status:</b> 404 Not Found
     * 
     * <p><b>Example Response:</b>
     * <pre>
     * {
     *   "error": "Transaction not found",
     *   "timestamp": "2026-01-22T10:30:00",
     *   "path": "/api/v1/transactions/invalid-id",
     *   "details": []
     * }
     * </pre>
     * 
     * @param ex The resource not found exception
     * @param request The HTTP request that caused the error
     * @return ResponseEntity with error details and HTTP 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        log.warn("Resource not found: {} - Request: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handles illegal argument exceptions.
     * 
     * <p>This handler is triggered when invalid arguments are passed to service
     * methods, such as invalid transaction types, negative amounts, etc.
     * 
     * <p><b>Response Status:</b> 400 Bad Request
     * 
     * <p><b>Example Response:</b>
     * <pre>
     * {
     *   "error": "Invalid transaction type: invalid",
     *   "timestamp": "2026-01-22T10:30:00",
     *   "path": "/api/v1/transactions",
     *   "details": []
     * }
     * </pre>
     * 
     * @param ex The illegal argument exception
     * @param request The HTTP request that caused the error
     * @return ResponseEntity with error details and HTTP 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        log.warn("Illegal argument: {} - Request: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handles rate limit exceeded exceptions.
     * 
     * <p>This handler is triggered when a client exceeds the rate limit
     * of 100 requests per minute.
     * 
     * <p><b>Response Status:</b> 429 Too Many Requests
     * 
     * <p><b>Response Headers:</b>
     * <ul>
     *   <li>X-RateLimit-Limit: Maximum requests allowed per minute</li>
     *   <li>X-RateLimit-Remaining: 0</li>
     *   <li>X-RateLimit-Reset: Unix timestamp when limit resets</li>
     *   <li>Retry-After: Seconds to wait before retrying</li>
     * </ul>
     * 
     * <p><b>Example Response:</b>
     * <pre>
     * {
     *   "error": "Rate limit exceeded. Maximum 100 requests per minute allowed.",
     *   "timestamp": "2026-01-22T10:30:00",
     *   "path": "/api/v1/transactions",
     *   "details": []
     * }
     * </pre>
     * 
     * @param ex The rate limit exceeded exception
     * @param request The HTTP request that caused the error
     * @return ResponseEntity with error details and HTTP 429 status
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(
            RateLimitExceededException ex,
            HttpServletRequest request) {
        
        log.warn("Rate limit exceeded for IP: {} - Request: {}", 
                request.getRemoteAddr(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Limit", String.valueOf(ex.getLimit()))
                .header("X-RateLimit-Remaining", String.valueOf(ex.getRemaining()))
                .header("X-RateLimit-Reset", String.valueOf(ex.getResetTime() / 1000))
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(errorResponse);
    }
    
    /**
     * Handles all other unexpected exceptions.
     * 
     * <p>This is a catch-all handler for any exception not explicitly handled
     * by other handlers. It logs the full stack trace and returns a generic
     * error message to avoid exposing sensitive information.
     * 
     * <p><b>Response Status:</b> 500 Internal Server Error
     * 
     * <p><b>Example Response:</b>
     * <pre>
     * {
     *   "error": "An unexpected error occurred",
     *   "timestamp": "2026-01-22T10:30:00",
     *   "path": "/api/v1/transactions",
     *   "details": []
     * }
     * </pre>
     * 
     * @param ex The unexpected exception
     * @param request The HTTP request that caused the error
     * @return ResponseEntity with error details and HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        log.error("Unexpected error occurred - Request: {}", request.getRequestURI(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                "An unexpected error occurred",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
