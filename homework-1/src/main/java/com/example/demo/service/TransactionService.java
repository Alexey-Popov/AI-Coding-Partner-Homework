package com.example.demo.service;

import com.example.demo.model.Transaction;
import com.example.demo.model.AccountSummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class TransactionService {
    private final Map<String, Transaction> transactions = new LinkedHashMap<>();
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^ACC-[A-Za-z0-9]{5}$");

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }

    public Optional<Transaction> getTransactionById(String id) {
        return Optional.ofNullable(transactions.get(id));
    }

    public List<Transaction> listTransactions(String accountId, String type, String from, String to) {
        List<Map<String, String>> details = new ArrayList<>();

        // Validate optional filters
        if (accountId != null && !ACCOUNT_PATTERN.matcher(accountId).matches()) {
            details.add(Map.of("field", "accountId", "message", "Account must follow format ACC-XXXXX (alphanumeric)"));
        }
        String normalizedType = null;
        if (type != null && !type.isBlank()) {
            normalizedType = type.toLowerCase(Locale.ROOT);
            if (!normalizedType.equals("deposit") && !normalizedType.equals("withdrawal") && !normalizedType.equals("transfer")) {
                details.add(Map.of("field", "type", "message", "Type must be one of deposit | withdrawal | transfer"));
            }
        }

        Instant fromInstant = null;
        Instant toInstant = null;
        try {
            if (from != null && !from.isBlank()) {
                fromInstant = parseDateOrInstantStart(from);
            }
            if (to != null && !to.isBlank()) {
                toInstant = parseDateOrInstantEnd(to);
            }
            if (fromInstant != null && toInstant != null && fromInstant.isAfter(toInstant)) {
                details.add(Map.of("field", "dateRange", "message", "'from' must be before or equal to 'to'"));
            }
        } catch (IllegalArgumentException ex) {
            details.add(Map.of("field", "date", "message", ex.getMessage()));
        }

        if (!details.isEmpty()) {
            throw new ValidationException(details);
        }

        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : transactions.values()) {
            // Status filter (only completed by default for history semantics)
            if (!"completed".equalsIgnoreCase(tx.getStatus())) {
                continue;
            }

            // Account filter: involved as sender or recipient
            if (accountId != null && !(accountId.equals(tx.getFromAccount()) || accountId.equals(tx.getToAccount()))) {
                continue;
            }

            // Type filter
            if (normalizedType != null) {
                String txType = tx.getType() == null ? "" : tx.getType().toLowerCase(Locale.ROOT);
                if (!txType.equals(normalizedType)) {
                    continue;
                }
            }

            // Date range filter (inclusive)
            if (fromInstant != null && (tx.getTimestamp() == null || tx.getTimestamp().isBefore(fromInstant))) {
                continue;
            }
            if (toInstant != null && (tx.getTimestamp() == null || tx.getTimestamp().isAfter(toInstant))) {
                continue;
            }

            result.add(tx);
        }
        return result;
    }

    private Instant parseDateOrInstantStart(String input) {
        // Accept ISO-8601 instant or a date (YYYY-MM-DD) treated as start of day UTC
        try {
            return Instant.parse(input);
        } catch (Exception ignored) {
        }
        try {
            LocalDate d = LocalDate.parse(input);
            return d.atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid 'from' date; use ISO-8601 or YYYY-MM-DD");
        }
    }

    private Instant parseDateOrInstantEnd(String input) {
        // Accept ISO-8601 instant or a date (YYYY-MM-DD) treated as end of day UTC
        try {
            return Instant.parse(input);
        } catch (Exception ignored) {
        }
        try {
            LocalDate d = LocalDate.parse(input);
            return d.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusMillis(1);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid 'to' date; use ISO-8601 or YYYY-MM-DD");
        }
    }

    public Transaction createTransaction(Transaction request) {
        List<Map<String, String>> details = new ArrayList<>();

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            details.add(Map.of("field", "amount", "message", "Amount must be a positive number"));
        } else if (amount.scale() > 2) {
            details.add(Map.of("field", "amount", "message", "Amount must have at most 2 decimal places"));
        }

        String from = request.getFromAccount();
        String to = request.getToAccount();
        if (from == null || !ACCOUNT_PATTERN.matcher(from).matches()) {
            details.add(Map.of("field", "fromAccount", "message", "Account must follow format ACC-XXXXX (alphanumeric)"));
        }
        if (to == null || !ACCOUNT_PATTERN.matcher(to).matches()) {
            details.add(Map.of("field", "toAccount", "message", "Account must follow format ACC-XXXXX (alphanumeric)"));
        }

        String currencyCode = request.getCurrency();
        try {
            if (currencyCode == null || currencyCode.isBlank()) {
                throw new IllegalArgumentException("Invalid currency code");
            }
            Currency.getInstance(currencyCode.toUpperCase());
        } catch (Exception e) {
            details.add(Map.of("field", "currency", "message", "Invalid currency code"));
        }

        if (!details.isEmpty()) {
            throw new ValidationException(details);
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

    public AccountSummary getAccountSummary(String accountId) {
        BigDecimal deposits = BigDecimal.ZERO;
        BigDecimal withdrawals = BigDecimal.ZERO;
        int count = 0;
        Instant mostRecent = null;

        for (Transaction tx : transactions.values()) {
            if (!"completed".equalsIgnoreCase(tx.getStatus())) {
                continue;
            }
            boolean involved = accountId.equals(tx.getFromAccount()) || accountId.equals(tx.getToAccount());
            if (!involved) {
                continue;
            }

            count++;

            if (tx.getTimestamp() != null) {
                if (mostRecent == null || tx.getTimestamp().isAfter(mostRecent)) {
                    mostRecent = tx.getTimestamp();
                }
            }

            String type = tx.getType() == null ? "" : tx.getType().toLowerCase(Locale.ROOT);
            if ("deposit".equals(type) && accountId.equals(tx.getToAccount())) {
                deposits = deposits.add(tx.getAmount());
            } else if ("withdrawal".equals(type) && accountId.equals(tx.getFromAccount())) {
                withdrawals = withdrawals.add(tx.getAmount());
            }
            // transfers are counted in transactionCount but excluded from deposit/withdraw totals
        }

        AccountSummary summary = new AccountSummary();
        summary.setTotalDeposits(deposits);
        summary.setTotalWithdrawals(withdrawals);
        summary.setTransactionCount(count);
        summary.setMostRecentTransactionDate(mostRecent);
        return summary;
    }
}
