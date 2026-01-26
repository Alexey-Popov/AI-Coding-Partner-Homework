package com.banking.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for account balance responses.
 * 
 * <p>This DTO is returned by the GET /api/v1/accounts/{accountId}/balance
 * endpoint to provide current account balance information.
 * 
 * <p>The balance is calculated by summing all completed transactions:
 * <ul>
 *   <li>DEPOSIT to the account: increases balance</li>
 *   <li>WITHDRAWAL from the account: decreases balance</li>
 *   <li>TRANSFER from the account: decreases balance</li>
 *   <li>TRANSFER to the account: increases balance</li>
 * </ul>
 * 
 * <p><b>Example JSON:</b>
 * <pre>
 * {
 *   "accountId": "ACC-12345",
 *   "balance": 5420.75,
 *   "currency": "USD",
 *   "calculatedAt": "2026-01-22T10:30:00"
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
public class BalanceResponse {
    
    /**
     * The account identifier (format: ACC-XXXXX).
     */
    private String accountId;
    
    /**
     * Current calculated balance for the account.
     * Can be negative if withdrawals exceed deposits.
     */
    private BigDecimal balance;
    
    /**
     * Currency code for the balance (ISO 4217 format).
     * Defaults to "USD" if no transactions exist for the account.
     */
    private String currency;
    
    /**
     * Timestamp when the balance was calculated.
     */
    private LocalDateTime calculatedAt;
}
