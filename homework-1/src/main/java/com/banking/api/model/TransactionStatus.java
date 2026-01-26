package com.banking.api.model;

/**
 * Enumeration representing the status of a banking transaction.
 * 
 * <p>Transaction lifecycle states:
 * <ul>
 *   <li><b>PENDING</b> - Transaction has been initiated but not yet processed</li>
 *   <li><b>COMPLETED</b> - Transaction has been successfully processed</li>
 *   <li><b>FAILED</b> - Transaction processing failed due to validation or business rule violations</li>
 * </ul>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
public enum TransactionStatus {
    /**
     * Transaction is pending processing.
     */
    PENDING,
    
    /**
     * Transaction has been successfully completed.
     */
    COMPLETED,
    
    /**
     * Transaction has failed and was not completed.
     */
    FAILED
}
