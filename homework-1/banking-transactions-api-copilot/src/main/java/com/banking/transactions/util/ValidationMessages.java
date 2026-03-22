package com.banking.transactions.util;

/**
 * Constants for validation error messages.
 * Centralizes all validation messages to maintain consistency.
 */
public final class ValidationMessages {
    
    // Prevent instantiation
    private ValidationMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // Account validation messages
    public static final String ACCOUNT_REQUIRED = "Account number is required";
    public static final String ACCOUNT_FORMAT = "Account number must follow format ACC-XXXXX (where X is alphanumeric)";
    
    // Amount validation messages
    public static final String AMOUNT_REQUIRED = "Amount is required";
    public static final String AMOUNT_POSITIVE = "Amount must be a positive number";
    public static final String AMOUNT_DECIMAL_PLACES = "Amount must have maximum 2 decimal places";
    
    // Currency validation messages
    public static final String CURRENCY_REQUIRED = "Currency is required";
    public static final String CURRENCY_INVALID = "Invalid currency code";
    
    // Type validation messages
    public static final String TYPE_REQUIRED = "Transaction type is required";
    public static final String TYPE_INVALID = "Transaction type must be one of: deposit, withdrawal, transfer";
    
    // Transaction validation messages
    public static final String TRANSACTION_DUPLICATE = "Duplicate transaction detected with same details";
    public static final String TRANSACTION_SAME_ACCOUNT = "From account and to account cannot be the same";
}
