package com.banking.model;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Serdeable
public class Transaction {

    private String id;

    @NotBlank(message = "fromAccount is required")
    @Pattern(regexp = "^ACC-[a-zA-Z0-9]+$", message = "fromAccount must match format ACC-<alphanumeric>")
    private String fromAccount;

    @NotBlank(message = "toAccount is required")
    @Pattern(regexp = "^ACC-[a-zA-Z0-9]+$", message = "toAccount must match format ACC-<alphanumeric>")
    private String toAccount;

    @NotNull(message = "amount is required")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency;

    @NotBlank(message = "type is required")
    @Pattern(regexp = "^(deposit|withdrawal|transfer)$", message = "type must be deposit, withdrawal, or transfer")
    private String type;

    private LocalDateTime timestamp;

    private String status;

    public Transaction() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.status = "completed";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }

    public String getToAccount() { return toAccount; }
    public void setToAccount(String toAccount) { this.toAccount = toAccount; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
