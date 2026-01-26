package com.banking.api.model;

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
public class Transaction {
    
    private String id;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private LocalDateTime timestamp;
    private TransactionStatus status;
}
