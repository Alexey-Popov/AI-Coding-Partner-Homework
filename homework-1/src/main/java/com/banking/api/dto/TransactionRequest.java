package com.banking.api.dto;

import com.banking.api.model.TransactionType;
import com.banking.api.validator.ValidAccountNumber;
import com.banking.api.validator.ValidAmount;
import com.banking.api.validator.ValidCurrency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    
    @ValidAccountNumber
    private String fromAccount;
    
    @ValidAccountNumber
    private String toAccount;
    
    @NotNull(message = "Amount is required")
    @ValidAmount
    private BigDecimal amount;
    
    @NotNull(message = "Currency is required")
    @ValidCurrency
    private String currency;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
}
