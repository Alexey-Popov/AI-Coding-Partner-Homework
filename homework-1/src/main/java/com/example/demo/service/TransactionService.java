package com.example.demo.service;

import com.example.demo.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class TransactionService {
    private final Map<String, Transaction> transactions = new LinkedHashMap<>();

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }

    public Optional<Transaction> getTransactionById(String id) {
        return Optional.ofNullable(transactions.get(id));
    }

    public Transaction createTransaction(Transaction request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be a positive number");
        }
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setFromAccount(request.getFromAccount());
        tx.setToAccount(request.getToAccount());
        tx.setAmount(request.getAmount());
        tx.setCurrency(request.getCurrency());
        tx.setType(request.getType());
        tx.setTimestamp(Instant.now());
        tx.setStatus("completed");
        transactions.put(tx.getId(), tx);
        return tx;
    }

    public BigDecimal getAccountBalance(String accountId) {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction tx : transactions.values()) {
            if (!"completed".equalsIgnoreCase(tx.getStatus())) {
                continue;
            }
            String type = tx.getType() == null ? "" : tx.getType().toLowerCase(Locale.ROOT);
            if ("deposit".equals(type)) {
                if (accountId.equals(tx.getToAccount())) {
                    balance = balance.add(tx.getAmount());
                }
            } else if ("withdrawal".equals(type)) {
                if (accountId.equals(tx.getFromAccount())) {
                    balance = balance.subtract(tx.getAmount());
                }
            } else if ("transfer".equals(type)) {
                if (accountId.equals(tx.getFromAccount())) {
                    balance = balance.subtract(tx.getAmount());
                }
                if (accountId.equals(tx.getToAccount())) {
                    balance = balance.add(tx.getAmount());
                }
            }
        }
        return balance;
    }
}
