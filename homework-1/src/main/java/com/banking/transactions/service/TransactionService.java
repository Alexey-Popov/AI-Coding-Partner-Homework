package com.banking.transactions.service;

import com.banking.transactions.dto.AccountSummary;
import com.banking.transactions.dto.ValidationErrorResponse;
import com.banking.transactions.exception.ValidationException;
import com.banking.transactions.model.Transaction;
import com.banking.transactions.model.Transaction.TransactionStatus;
import com.banking.transactions.model.Transaction.TransactionType;
import com.banking.transactions.util.CurrencyValidator;
import com.banking.transactions.util.ValidationMessages;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service class for managing banking transactions.
 * Handles transaction creation, validation, filtering, and statistics.
 */
@Service
public class TransactionService {
    
    private final ConcurrentHashMap<String, Transaction> transactions = new ConcurrentHashMap<>();

    /**
     * Creates a new transaction with comprehensive validation.
     * Validates all required fields, checks for duplicates, and generates a unique ID.
     *
     * @param transaction the transaction to create
     * @return the created transaction with generated ID and timestamp
     * @throws ValidationException if any validation rule fails
     */
    public Transaction createTransaction(Transaction transaction) {
        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        
        // Validate required field: fromAccount
        if (transaction.getFromAccount() == null || transaction.getFromAccount().trim().isEmpty()) {
            errorResponse.addDetail("fromAccount", ValidationMessages.ACCOUNT_REQUIRED);
        } else if (!isValidAccountFormat(transaction.getFromAccount())) {
            errorResponse.addDetail("fromAccount", ValidationMessages.ACCOUNT_FORMAT);
        }
        
        // Validate required field: toAccount
        if (transaction.getToAccount() == null || transaction.getToAccount().trim().isEmpty()) {
            errorResponse.addDetail("toAccount", ValidationMessages.ACCOUNT_REQUIRED);
        } else if (!isValidAccountFormat(transaction.getToAccount())) {
            errorResponse.addDetail("toAccount", ValidationMessages.ACCOUNT_FORMAT);
        }
        
        // Validate required field: amount
        if (transaction.getAmount() == null) {
            errorResponse.addDetail("amount", ValidationMessages.AMOUNT_REQUIRED);
        } else if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errorResponse.addDetail("amount", ValidationMessages.AMOUNT_POSITIVE);
        } else if (transaction.getAmount().scale() > 2) {
            errorResponse.addDetail("amount", ValidationMessages.AMOUNT_DECIMAL_PLACES);
        }
        
        // Validate required field: currency
        if (transaction.getCurrency() == null || transaction.getCurrency().trim().isEmpty()) {
            errorResponse.addDetail("currency", ValidationMessages.CURRENCY_REQUIRED);
        } else if (!CurrencyValidator.isValidCurrency(transaction.getCurrency())) {
            errorResponse.addDetail("currency", ValidationMessages.CURRENCY_INVALID);
        }
        
        // Validate required field: type
        if (transaction.getType() == null) {
            errorResponse.addDetail("type", ValidationMessages.TYPE_REQUIRED);
        } else {
            // Validate type is one of the allowed values
            if (transaction.getType() != Transaction.TransactionType.DEPOSIT &&
                transaction.getType() != Transaction.TransactionType.WITHDRAWAL &&
                transaction.getType() != Transaction.TransactionType.TRANSFER) {
                errorResponse.addDetail("type", ValidationMessages.TYPE_INVALID);
            }
        }
        
        // Validate no duplicate accounts for all transaction types
        if (transaction.getFromAccount() != null && transaction.getToAccount() != null &&
            transaction.getFromAccount().equals(transaction.getToAccount())) {
            errorResponse.addDetail("accounts", ValidationMessages.TRANSACTION_SAME_ACCOUNT);
        }
        
        // If there are validation errors, throw exception
        if (!errorResponse.getDetails().isEmpty()) {
            throw new ValidationException(errorResponse);
        }
        
        // Check for duplicate transaction
        if (isDuplicateTransaction(transaction)) {
            errorResponse.addDetail("transaction", ValidationMessages.TRANSACTION_DUPLICATE);
            throw new ValidationException(errorResponse);
        }
        
        // Generate unique ID
        String id = UUID.randomUUID().toString();
        transaction.setId(id);
        
        // Set status to completed for simplicity
        transaction.setStatus(TransactionStatus.COMPLETED);
        
        // Store transaction
        transactions.put(id, transaction);
        
        return transaction;
    }

    /**
     * Retrieves all transactions from the in-memory store.
     *
     * @return list of all transactions
     */
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }

    /**
     * Retrieves transactions filtered by optional criteria.
     * All filters can be combined. Filters are case-insensitive.
     *
     * @param accountId optional account ID (matches fromAccount or toAccount)
     * @param type optional transaction type (deposit, withdrawal, transfer)
     * @param from optional start date in ISO 8601 format (YYYY-MM-DD)
     * @param to optional end date in ISO 8601 format (YYYY-MM-DD)
     * @return list of transactions matching all provided filters
     */
    public List<Transaction> getFilteredTransactions(String accountId, String type, String from, String to) {
        List<Transaction> result = new ArrayList<>(transactions.values());
        
        // Filter by accountId (matches either fromAccount or toAccount)
        if (accountId != null && !accountId.trim().isEmpty()) {
            result = result.stream()
                .filter(t -> accountId.equals(t.getFromAccount()) || accountId.equals(t.getToAccount()))
                .collect(Collectors.toList());
        }
        
        // Filter by type
        if (type != null && !type.trim().isEmpty()) {
            Transaction.TransactionType transactionType = parseTransactionType(type);
            if (transactionType != null) {
                result = result.stream()
                    .filter(t -> t.getType() == transactionType)
                    .collect(Collectors.toList());
            }
        }
        
        // Filter by date range
        if (from != null && !from.trim().isEmpty()) {
            LocalDate fromDate = parseDate(from);
            if (fromDate != null) {
                LocalDateTime fromDateTime = fromDate.atStartOfDay();
                result = result.stream()
                    .filter(t -> t.getTimestamp() != null && !t.getTimestamp().isBefore(fromDateTime))
                    .collect(Collectors.toList());
            }
        }
        
        if (to != null && !to.trim().isEmpty()) {
            LocalDate toDate = parseDate(to);
            if (toDate != null) {
                LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
                result = result.stream()
                    .filter(t -> t.getTimestamp() != null && !t.getTimestamp().isAfter(toDateTime))
                    .collect(Collectors.toList());
            }
        }
        
        return result;
    }

    /**
     * Retrieves a transaction by its unique identifier.
     *
     * @param id the unique identifier of the transaction
     * @return the transaction if found, null otherwise
     */
    public Transaction getTransactionById(String id) {
        return transactions.get(id);
    }
    
    private Transaction.TransactionType parseTransactionType(String type) {
        try {
            return Transaction.TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    private boolean isValidAccountFormat(String accountNumber) {
        // Account format: ACC-XXXXX where X is alphanumeric (A-Z, a-z, 0-9)
        return accountNumber != null && accountNumber.matches("^ACC-[A-Za-z0-9]{5}$");
    }
    
    private boolean isDuplicateTransaction(Transaction newTransaction) {
        // Check if identical transaction already exists (within last 5 minutes to prevent accidental duplicates)
        long fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000);
        
        for (Transaction existing : transactions.values()) {
            if (existing.getFromAccount() != null && existing.getFromAccount().equals(newTransaction.getFromAccount()) &&
                existing.getToAccount() != null && existing.getToAccount().equals(newTransaction.getToAccount()) &&
                existing.getAmount() != null && existing.getAmount().compareTo(newTransaction.getAmount()) == 0 &&
                existing.getCurrency() != null && existing.getCurrency().equals(newTransaction.getCurrency()) &&
                existing.getType() == newTransaction.getType()) {
                
                // Check if the existing transaction was created within the last 5 minutes
                if (existing.getTimestamp() != null) {
                    long existingTimestamp = java.time.ZoneId.systemDefault()
                        .getRules()
                        .getOffset(existing.getTimestamp())
                        .getTotalSeconds() * 1000L + 
                        existing.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                    
                    if (existingTimestamp > fiveMinutesAgo) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Generates a statistical summary for a specific account.
     * Includes counts of deposits, withdrawals, total transactions, and most recent date.
     *
     * @param accountId the account ID to generate summary for
     * @return AccountSummary containing transaction statistics
     */
    public AccountSummary getAccountSummary(String accountId) {
        // Get all completed transactions for this account
        List<Transaction> accountTransactions = transactions.values().stream()
            .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
            .filter(t -> (t.getFromAccount() != null && t.getFromAccount().equals(accountId)) ||
                        (t.getToAccount() != null && t.getToAccount().equals(accountId)))
            .collect(Collectors.toList());

        // Count deposit transactions only (type = DEPOSIT)
        int totalDeposits = (int) accountTransactions.stream()
            .filter(t -> t.getType() == TransactionType.DEPOSIT)
            .count();

        // Count withdrawal transactions only (type = WITHDRAWAL)
        int totalWithdrawals = (int) accountTransactions.stream()
            .filter(t -> t.getType() == TransactionType.WITHDRAWAL)
            .count();

        // Get number of transactions
        int numberOfTransactions = accountTransactions.size();

        // Get most recent transaction date
        LocalDateTime mostRecentDate = accountTransactions.stream()
            .map(Transaction::getTimestamp)
            .filter(timestamp -> timestamp != null)
            .max(Comparator.naturalOrder())
            .orElse(null);

        return new AccountSummary(totalDeposits, totalWithdrawals, numberOfTransactions, mostRecentDate);
    }
}
