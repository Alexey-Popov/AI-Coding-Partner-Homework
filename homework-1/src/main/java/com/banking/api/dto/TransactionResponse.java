package com.banking.api.dto;

import com.banking.api.model.TransactionStatus;
import com.banking.api.model.TransactionType;
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
public class TransactionResponse {
    
    private String id;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private LocalDateTime timestamp;
    private TransactionStatus status;
}
