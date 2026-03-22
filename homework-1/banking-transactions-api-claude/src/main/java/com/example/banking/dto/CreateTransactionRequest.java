package com.example.banking.dto;

import com.example.banking.model.TransactionType;
import com.example.banking.validation.DecimalPlaces;
import com.example.banking.validation.ValidAccountNumber;
import com.example.banking.validation.ValidCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CreateTransactionRequest {

    @NotBlank(message = "From account is required")
    @ValidAccountNumber
    private String fromAccount;

    @NotBlank(message = "To account is required")
    @ValidAccountNumber
    private String toAccount;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalPlaces(max = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @ValidCurrency
    private String currency;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
