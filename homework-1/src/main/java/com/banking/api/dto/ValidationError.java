package com.banking.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for validation error details.
 * 
 * <p>This DTO represents a single validation error, typically used
 * within the {@link ErrorResponse} to provide field-specific validation
 * failure information.
 * 
 * <p><b>Example JSON:</b>
 * <pre>
 * {
 *   "field": "amount",
 *   "message": "Amount must be a positive number"
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
public class ValidationError {
    
    /**
     * The name of the field that failed validation.
     * Examples: "amount", "currency", "fromAccount"
     */
    private String field;
    
    /**
     * A human-readable message describing the validation error.
     * Should be clear and actionable for API consumers.
     */
    private String message;
}
