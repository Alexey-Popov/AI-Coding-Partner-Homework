package com.banking.transactions.model;

import com.banking.transactions.validation.ValidAccountNumber;
import com.banking.transactions.validation.ValidCurrency;
import com.banking.transactions.validation.ValidDecimalPlaces;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    
    private String id;
    
    @ValidAccountNumber
    private String fromAccount;
    
    @ValidAccountNumber
    private String toAccount;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be a positive number")
    @ValidDecimalPlaces(max = 2, message = "Amount must have maximum 2 decimal places")
    private BigDecimal amount;
    
    @NotNull(message = "Currency is required")
    @ValidCurrency
    private String currency;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @NotNull(message = "Status is required")
    private TransactionStatus status;

    // Constructors
    public Transaction() {
    }

    public Transaction(String id, String fromAccount, String toAccount, BigDecimal amount, 
                      String currency, TransactionType type, LocalDateTime timestamp, 
                      TransactionStatus status) {
        this.id = id;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", fromAccount='" + fromAccount + '\'' +
                ", toAccount='" + toAccount + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", type=" + type +
                ", timestamp=" + timestamp +
                ", status=" + status +
                '}';
    }
}
