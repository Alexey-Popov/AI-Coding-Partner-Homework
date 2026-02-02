package com.banking.service;

import com.banking.model.Transaction;
import com.banking.repository.TransactionRepository;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Singleton
public class TransactionService {

    private static final Set<String> VALID_CURRENCIES = Set.of(
            "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD", "CNY", "INR",
            "BRL", "KRW", "MXN", "SGD", "HKD", "NOK", "SEK", "DKK", "PLN", "ZAR",
            "THB", "TWD", "TRY", "RUB", "ILS", "CZK", "HUF", "CLP", "PHP", "AED",
            "COP", "SAR", "MYR", "IDR", "RON", "BGN", "HRK", "PEN", "UAH", "ARS",
            "VND", "EGP", "PKR", "NGN", "BDT", "KES", "QAR", "KWD", "BHD", "OMR"
    );

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<String> validate(Transaction transaction) {
        List<String> errors = new ArrayList<>();
        if (transaction.getCurrency() != null && !VALID_CURRENCIES.contains(transaction.getCurrency().toUpperCase())) {
            errors.add("Invalid ISO 4217 currency code: " + transaction.getCurrency());
        }
        return errors;
    }

    public Transaction create(Transaction transaction) {
        return repository.save(transaction);
    }

    public Optional<Transaction> findById(String id) {
        return repository.findById(id);
    }

    public List<Transaction> findAll(String accountId, String type, LocalDateTime from, LocalDateTime to) {
        return repository.findAll(accountId, type, from, to);
    }

    public BigDecimal getBalance(String accountId) {
        List<Transaction> transactions = repository.findByAccount(accountId);
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (!"completed".equals(t.getStatus())) continue;
            boolean isTo = accountId.equals(t.getToAccount());
            boolean isFrom = accountId.equals(t.getFromAccount());
            switch (t.getType()) {
                case "deposit":
                    if (isTo) balance = balance.add(t.getAmount());
                    break;
                case "withdrawal":
                    if (isFrom) balance = balance.subtract(t.getAmount());
                    break;
                case "transfer":
                    if (isTo) balance = balance.add(t.getAmount());
                    if (isFrom) balance = balance.subtract(t.getAmount());
                    break;
            }
        }
        return balance;
    }

    public Map<String, Object> getSummary(String accountId) {
        List<Transaction> transactions = repository.findByAccount(accountId);
        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        LocalDateTime mostRecent = null;

        for (Transaction t : transactions) {
            if ("deposit".equals(t.getType())) {
                totalDeposits = totalDeposits.add(t.getAmount());
            } else if ("withdrawal".equals(t.getType())) {
                totalWithdrawals = totalWithdrawals.add(t.getAmount());
            }
            if (mostRecent == null || t.getTimestamp().isAfter(mostRecent)) {
                mostRecent = t.getTimestamp();
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("accountId", accountId);
        summary.put("totalDeposits", totalDeposits);
        summary.put("totalWithdrawals", totalWithdrawals);
        summary.put("transactionCount", transactions.size());
        summary.put("mostRecentTransaction", mostRecent);
        return summary;
    }
}
