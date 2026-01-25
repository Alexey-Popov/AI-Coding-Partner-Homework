package com.banking.transactions.repository;

import com.banking.transactions.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTransactionRepository {
    
    private final ConcurrentHashMap<String, Transaction> transactions = new ConcurrentHashMap<>();

    /**
     * Save a transaction to the repository
     */
    public Transaction save(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    /**
     * Find all transactions
     */
    public List<Transaction> findAll() {
        return new ArrayList<>(transactions.values());
    }

    /**
     * Find a transaction by ID
     */
    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(transactions.get(id));
    }

    /**
     * Check if a transaction exists by ID
     */
    public boolean existsById(String id) {
        return transactions.containsKey(id);
    }

    /**
     * Delete a transaction by ID
     */
    public void deleteById(String id) {
        transactions.remove(id);
    }

    /**
     * Clear all transactions (useful for testing)
     */
    public void clear() {
        transactions.clear();
    }

    /**
     * Count total transactions
     */
    public long count() {
        return transactions.size();
    }
}
