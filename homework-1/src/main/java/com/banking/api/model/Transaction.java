package com.banking.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a banking transaction.
 * 
 * <p>This entity encapsulates all information related to a financial transaction,
 * including source and destination accounts, monetary amount, currency, transaction
 * type, status, and timestamp.
 * 
 * <p><b>Validation Rules:</b>
 * <ul>
 *   <li>ID is auto-generated using UUID</li>
 *   <li>Account numbers must follow format: ACC-XXXXX (where X is alphanumeric)</li>
 *   <li>Amount must be positive with maximum 2 decimal places</li>
 *   <li>Currency must be a valid ISO 4217 code (USD, EUR, GBP, JPY, etc.)</li>
 *   <li>Timestamp is in ISO 8601 format</li>
 * </ul>
 * 
 * <p><b>Transaction Types:</b>
 * <ul>
 *   <li><b>DEPOSIT</b> - Only toAccount is used, fromAccount can be null</li>
 *   <li><b>WITHDRAWAL</b> - Only fromAccount is used, toAccount can be null</li>
 *   <li><b>TRANSFER</b> - Both fromAccount and toAccount are required</li>
 * </ul>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    
    /**
     * Unique identifier for the transaction.
     * Auto-generated using UUID when transaction is created.
     */
    private String id;
    
    /**
     * Source account identifier for withdrawals and transfers.
     * Format: ACC-XXXXX (e.g., ACC-12345)
     * Null for deposit transactions.
     */
    private String fromAccount;
    
    /**
     * Destination account identifier for deposits and transfers.
     * Format: ACC-XXXXX (e.g., ACC-67890)
     * Null for withdrawal transactions.
     */
    private String toAccount;
    
    /**
     * Transaction amount with maximum 2 decimal places.
     * Must be positive.
     * Uses BigDecimal for precise monetary calculations.
     */
    private BigDecimal amount;
    
    /**
     * ISO 4217 currency code (e.g., USD, EUR, GBP, JPY).
     * Must be a valid 3-letter currency code.
     */
    private String currency;
    
    /**
     * Type of transaction: DEPOSIT, WITHDRAWAL, or TRANSFER.
     */
    private TransactionType type;
    
    /**
     * Timestamp when the transaction was created or processed.
     * Stored in ISO 8601 format (e.g., 2026-01-22T10:30:00Z).
     */
    private LocalDateTime timestamp;
    
    /**
     * Current status of the transaction: PENDING, COMPLETED, or FAILED.
     */
    private TransactionStatus status;
    
    /**
     * Generates a new unique transaction ID using UUID.
     * 
     * @return A new UUID string
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Checks if this transaction involves the specified account.
     * An account is involved if it appears as either fromAccount or toAccount.
     * 
     * @param accountId The account ID to check
     * @return true if the account is involved in this transaction, false otherwise
     */
    public boolean involvesAccount(String accountId) {
        if (accountId == null) {
            return false;
        }
        return accountId.equals(fromAccount) || accountId.equals(toAccount);
    }
    
    /**
     * Checks if the transaction timestamp falls within the specified date range (inclusive).
     * 
     * @param from Start date of the range (null means no lower bound)
     * @param to End date of the range (null means no upper bound)
     * @return true if the transaction timestamp is within the range, false otherwise
     */
    public boolean isWithinDateRange(LocalDateTime from, LocalDateTime to) {
        if (timestamp == null) {
            return false;
        }
        
        boolean afterFrom = from == null || !timestamp.isBefore(from);
        boolean beforeTo = to == null || !timestamp.isAfter(to);
        
        return afterFrom && beforeTo;
    }
}
