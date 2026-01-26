package com.banking.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {
    
    private String accountId;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private int numberOfTransactions;
    private LocalDateTime mostRecentTransactionDate;
}
