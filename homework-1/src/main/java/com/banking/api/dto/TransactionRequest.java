package com.banking.api.dto;

import com.banking.api.validator.ValidAccountNumber;
import com.banking.api.validator.ValidAmount;
import com.banking.api.validator.ValidCurrency;
import com.banking.api.validator.ValidTransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating new transactions.
 * 
 * <p>This DTO is used in the POST /api/v1/transactions endpoint to accept
 * transaction creation requests from clients.
 * 
 * <p><b>Validation Requirements:</b>
 * <ul>
 *   <li>fromAccount: Required for WITHDRAWAL and TRANSFER, null for DEPOSIT</li>
 *   <li>toAccount: Required for DEPOSIT and TRANSFER, null for WITHDRAWAL</li>
 *   <li>amount: Must be positive with maximum 2 decimal places</li>
 *   <li>currency: Must be valid ISO 4217 code (USD, EUR, GBP, JPY, etc.)</li>
 *   <li>type: Must be one of: deposit, withdrawal, transfer (case-insensitive)</li>
 * </ul>
 * 
 * <p><b>Example JSON:</b>
 * <pre>
 * {
 *   "fromAccount": "ACC-12345",
 *   "toAccount": "ACC-67890",
 *   "amount": 100.50,
 *   "currency": "USD",
 *   "type": "transfer"
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
public class TransactionRequest {
    
    /**
     * Source account for withdrawals and transfers.
     * Format: ACC-XXXXX (e.g., ACC-12345)
     * Should be null for deposit transactions.
     */
    @ValidAccountNumber
    private String fromAccount;
    
    /**
     * Destination account for deposits and transfers.
     * Format: ACC-XXXXX (e.g., ACC-67890)
     * Should be null for withdrawal transactions.
     */
    @ValidAccountNumber
    private String toAccount;
    
    /**
     * Transaction amount.
     * Must be positive with maximum 2 decimal places.
     */
    @NotNull(message = "Amount is required")
    @ValidAmount
    private BigDecimal amount;
    
    /**
     * ISO 4217 currency code (e.g., USD, EUR, GBP, JPY).
     * Must be a valid 3-letter currency code.
     */
    @NotNull(message = "Currency is required")
    @ValidCurrency
    private String currency;
    
    /**
     * Type of transaction: deposit, withdrawal, or transfer.
     * Stored as lowercase string, converted to TransactionType enum internally.
     */
    @NotNull(message = "Transaction type is required")
    @ValidTransactionType
    private String type;
}
