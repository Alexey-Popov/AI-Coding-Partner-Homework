package com.banking.api.dto;

import com.banking.api.model.Transaction;
import com.banking.api.model.TransactionStatus;
import com.banking.api.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for transaction responses.
 * 
 * <p>This DTO is returned by all transaction endpoints to provide
 * a consistent response format. It includes all transaction details.
 * 
 * <p><b>Used by Endpoints:</b>
 * <ul>
 *   <li>POST /api/v1/transactions - Returns single transaction</li>
 *   <li>GET /api/v1/transactions - Returns list of transactions</li>
 *   <li>GET /api/v1/transactions/{id} - Returns single transaction</li>
 * </ul>
 * 
 * <p><b>Example JSON:</b>
 * <pre>
 * {
 *   "id": "550e8400-e29b-41d4-a716-446655440000",
 *   "fromAccount": "ACC-12345",
 *   "toAccount": "ACC-67890",
 *   "amount": 100.50,
 *   "currency": "USD",
 *   "type": "TRANSFER",
 *   "timestamp": "2026-01-22T10:30:00",
 *   "status": "COMPLETED"
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
public class TransactionResponse {
    
    /**
     * Unique transaction identifier (UUID).
     */
    private String id;
    
    /**
     * Source account identifier (format: ACC-XXXXX).
     * Null for deposit transactions.
     */
    private String fromAccount;
    
    /**
     * Destination account identifier (format: ACC-XXXXX).
     * Null for withdrawal transactions.
     */
    private String toAccount;
    
    /**
     * Transaction amount with maximum 2 decimal places.
     */
    private BigDecimal amount;
    
    /**
     * ISO 4217 currency code (e.g., USD, EUR, GBP).
     */
    private String currency;
    
    /**
     * Transaction type: DEPOSIT, WITHDRAWAL, or TRANSFER.
     */
    private TransactionType type;
    
    /**
     * Transaction timestamp in ISO 8601 format.
     */
    private LocalDateTime timestamp;
    
    /**
     * Transaction status: PENDING, COMPLETED, or FAILED.
     */
    private TransactionStatus status;
    
    /**
     * Converts a Transaction entity to a TransactionResponse DTO.
     * 
     * @param transaction The transaction entity to convert
     * @return TransactionResponse DTO with all transaction data
     */
    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .fromAccount(transaction.getFromAccount())
                .toAccount(transaction.getToAccount())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type(transaction.getType())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus())
                .build();
    }
}
