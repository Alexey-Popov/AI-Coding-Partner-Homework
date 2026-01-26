package com.banking.api.model;

/**
 * Enumeration representing the type of banking transaction.
 * 
 * <p>Supported transaction types:
 * <ul>
 *   <li><b>DEPOSIT</b> - Money added to an account</li>
 *   <li><b>WITHDRAWAL</b> - Money removed from an account</li>
 *   <li><b>TRANSFER</b> - Money moved between two accounts</li>
 * </ul>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
public enum TransactionType {
    /**
     * Represents a deposit transaction where money is added to an account.
     */
    DEPOSIT,
    
    /**
     * Represents a withdrawal transaction where money is removed from an account.
     */
    WITHDRAWAL,
    
    /**
     * Represents a transfer transaction where money is moved between accounts.
     */
    TRANSFER
}
