package com.banking.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Transaction Summary Response
 * Provides comprehensive account statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {
    
    /**
     * Account ID for which summary is generated
     */
    private String accountId;
    
    /**
     * Total amount of all deposits to this account
     */
    private BigDecimal totalDeposits;
    
    /**
     * Total amount of all withdrawals from this account
     */
    private BigDecimal totalWithdrawals;
    
    /**
     * Total number of transactions involving this account
     */
    private Integer numberOfTransactions;
    
    /**
     * Timestamp of the most recent transaction
     */
    private LocalDateTime mostRecentTransactionDate;
    
    /**
     * Current calculated balance for this account
     */
    private BigDecimal currentBalance;
    
    /**
     * Currency code (typically from transactions)
     */
    private String currency;
}
