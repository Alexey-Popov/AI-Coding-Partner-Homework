package com.banking.api.exception;

/**
 * Custom exception thrown when a requested resource is not found.
 * 
 * <p>This exception is used throughout the application to indicate
 * that a resource (e.g., transaction, account) could not be found.
 * 
 * <p>The {@link GlobalExceptionHandler} catches this exception and
 * returns an HTTP 404 Not Found response.
 * 
 * <p><b>Usage Example:</b>
 * <pre>
 * Optional&lt;Transaction&gt; transaction = repository.findById(id);
 * if (transaction.isEmpty()) {
 *     throw new ResourceNotFoundException("Transaction not found with ID: " + id);
 * }
 * </pre>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     * 
     * @param message The detail message explaining which resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     * 
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
