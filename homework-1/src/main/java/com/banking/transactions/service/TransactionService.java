package com.banking.transactions.service;

import com.banking.transactions.dto.AccountBalanceResponse;
import com.banking.transactions.model.Transaction;
import com.banking.transactions.model.TransactionStatus;
import com.banking.transactions.model.TransactionType;
import com.banking.transactions.repository.InMemoryTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    
    private final InMemoryTransactionRepository repository;

    public TransactionService(InMemoryTransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a new transaction
     */
    public Transaction createTransaction(Transaction transaction) {
        // Generate ID and timestamp
        transaction.setId(UUID.randomUUID().toString());
        transaction.setTimestamp(LocalDateTime.now());
        
        return repository.save(transaction);
    }

    /**
     * Get all transactions with optional filters
     */
    public List<Transaction> getTransactions(String accountId, String type, LocalDate from, LocalDate to) {
        List<Transaction> transactions = repository.findAll();
        
        // Apply filters
        if (accountId != null && !accountId.isBlank()) {
            transactions = filterByAccount(transactions, accountId);
        }
        
        if (type != null && !type.isBlank()) {
            transactions = filterByType(transactions, type);
        }
        
        if (from != null || to != null) {
            transactions = filterByDateRange(transactions, from, to);
        }
        
        return transactions;
    }

    /**
     * Get a transaction by ID
     */
    public Optional<Transaction> getTransactionById(String id) {
        return repository.findById(id);
    }

    /**
     * Get account balance
     */
    public AccountBalanceResponse getAccountBalance(String accountId) {
        List<Transaction> accountTransactions = repository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .filter(t -> accountMatchesTransaction(accountId, t))
                .collect(Collectors.toList());
        
        // Determine currency from first transaction
        String currency = accountTransactions.stream()
                .findFirst()
                .map(Transaction::getCurrency)
                .orElse("USD"); // Default currency if no transactions
        
        // Calculate balance: deposits add, withdrawals and transfers subtract
        BigDecimal balance = accountTransactions.stream()
                .map(t -> calculateTransaction(t, accountId))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new AccountBalanceResponse(accountId, balance, currency);
    }

    // Helper methods

    private List<Transaction> filterByAccount(List<Transaction> transactions, String accountId) {
        return transactions.stream()
                .filter(t -> accountMatchesTransaction(accountId, t))
                .collect(Collectors.toList());
    }

    private boolean accountMatchesTransaction(String accountId, Transaction transaction) {
        return accountId.equals(transaction.getFromAccount()) || 
               accountId.equals(transaction.getToAccount());
    }

    private List<Transaction> filterByType(List<Transaction> transactions, String type) {
        try {
            TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
            return transactions.stream()
                    .filter(t -> t.getType() == transactionType)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            // Invalid type, return all transactions
            return transactions;
        }
    }

    private List<Transaction> filterByDateRange(List<Transaction> transactions, LocalDate from, LocalDate to) {
        return transactions.stream()
                .filter(t -> {
                    LocalDate transactionDate = t.getTimestamp().toLocalDate();
                    
                    if (from != null && to != null) {
                        return !transactionDate.isBefore(from) && !transactionDate.isAfter(to);
                    } else if (from != null) {
                        return !transactionDate.isBefore(from);
                    } else if (to != null) {
                        return !transactionDate.isAfter(to);
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTransaction(Transaction transaction, String accountId) {
        BigDecimal amount = transaction.getAmount();
        
        // DEPOSIT: adds to balance (when toAccount matches)
        // WITHDRAWAL: subtracts from balance (when fromAccount matches)
        // TRANSFER: subtracts from balance (when fromAccount matches)
        
        if (transaction.getType() == TransactionType.DEPOSIT) {
            return accountId.equals(transaction.getToAccount()) ? amount : BigDecimal.ZERO;
        } else if (transaction.getType() == TransactionType.WITHDRAWAL || 
                   transaction.getType() == TransactionType.TRANSFER) {
            return accountId.equals(transaction.getFromAccount()) ? amount.negate() : BigDecimal.ZERO;
        }
        
        return BigDecimal.ZERO;
    }
}
