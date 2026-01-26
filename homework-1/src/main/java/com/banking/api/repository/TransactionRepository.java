package com.banking.api.repository;

import com.banking.api.model.Transaction;
import com.banking.api.model.TransactionType;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository {
    
    private final ConcurrentHashMap<String, Transaction> transactions = new ConcurrentHashMap<>();
    
    public Transaction save(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }
    
    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(transactions.get(id));
    }
    
    public List<Transaction> findAll() {
        return List.copyOf(transactions.values());
    }
    
    public List<Transaction> findByAccountId(String accountId) {
        return transactions.values().stream()
                .filter(t -> accountId.equals(t.getFromAccount()) || accountId.equals(t.getToAccount()))
                .collect(Collectors.toList());
    }
    
    public List<Transaction> findByType(TransactionType type) {
        return transactions.values().stream()
                .filter(t -> type.equals(t.getType()))
                .collect(Collectors.toList());
    }
    
    public List<Transaction> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return transactions.values().stream()
                .filter(t -> !t.getTimestamp().isBefore(from) && !t.getTimestamp().isAfter(to))
                .collect(Collectors.toList());
    }
    
    public List<Transaction> findWithFilters(String accountId, TransactionType type, 
                                             LocalDateTime from, LocalDateTime to) {
        return transactions.values().stream()
                .filter(t -> accountId == null || 
                        accountId.equals(t.getFromAccount()) || accountId.equals(t.getToAccount()))
                .filter(t -> type == null || type.equals(t.getType()))
                .filter(t -> from == null || !t.getTimestamp().isBefore(from))
                .filter(t -> to == null || !t.getTimestamp().isAfter(to))
                .collect(Collectors.toList());
    }
}
