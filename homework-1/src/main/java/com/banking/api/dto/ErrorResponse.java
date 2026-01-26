package com.banking.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for error responses.
 * 
 * <p>This DTO provides a consistent error response format across
 * all API endpoints. It includes general error information and
 * optional field-specific validation details.
 * 
 * <p><b>Used For:</b>
 * <ul>
 *   <li>400 Bad Request - Validation errors</li>
 *   <li>404 Not Found - Resource not found</li>
 *   <li>500 Internal Server Error - Unexpected errors</li>
 * </ul>
 * 
 * <p><b>Example JSON (Validation Error):</b>
 * <pre>
 * {
 *   "error": "Validation failed",
 *   "timestamp": "2026-01-22T10:30:00",
 *   "path": "/api/v1/transactions",
 *   "details": [
 *     {
 *       "field": "amount",
 *       "message": "Amount must be a positive number"
 *     },
 *     {
 *       "field": "currency",
 *       "message": "Invalid currency code"
 *     }
 *   ]
 * }
 * </pre>
 * 
 * <p><b>Example JSON (Not Found):</b>
 * <pre>
 * {
 *   "error": "Transaction not found",
 *   "timestamp": "2026-01-22T10:30:00",
 *   "path": "/api/v1/transactions/invalid-id",
 *   "details": []
 * }
 * </pre>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    
    /**
     * A brief description of the error.
     * Examples: "Validation failed", "Transaction not found", "Internal server error"
     */
    private String error;
    
    /**
     * The timestamp when the error occurred.
     */
    private LocalDateTime timestamp;
    
    /**
     * The request path that caused the error.
     * Example: "/api/v1/transactions"
     */
    private String path;
    
    /**
     * A list of detailed validation errors.
     * Empty list if the error is not validation-related.
     */
    @Builder.Default
    private List<ValidationError> details = new ArrayList<>();
    
    /**
     * Creates a simple error response without validation details.
     * 
     * @param error The error message
     * @param path The request path
     * @return ErrorResponse with timestamp set to now and empty details list
     */
    public static ErrorResponse of(String error, String path) {
        return ErrorResponse.builder()
                .error(error)
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(new ArrayList<>())
                .build();
    }
    
    /**
     * Creates an error response with validation details.
     * 
     * @param error The error message
     * @param path The request path
     * @param details List of validation errors
     * @return ErrorResponse with timestamp set to now
     */
    public static ErrorResponse of(String error, String path, List<ValidationError> details) {
        return ErrorResponse.builder()
                .error(error)
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(details != null ? details : new ArrayList<>())
                .build();
    }
}
