package com.banking.api.repository;

import com.banking.api.model.Transaction;
import com.banking.api.model.TransactionType;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository for managing transaction data in memory.
 * 
 * <p>This repository provides thread-safe in-memory storage for transactions
 * using {@link ConcurrentHashMap}. All operations are safe for concurrent access.
 * 
 * <p><b>Storage Strategy:</b>
 * <ul>
 *   <li>Thread-safe using ConcurrentHashMap</li>
 *   <li>Key: Transaction ID (String)</li>
 *   <li>Value: Transaction object</li>
 *   <li>No persistence - data lost on application restart</li>
 * </ul>
 * 
 * <p><b>Query Capabilities:</b>
 * <ul>
 *   <li>Find by ID</li>
 *   <li>Find by account (fromAccount or toAccount)</li>
 *   <li>Find by transaction type</li>
 *   <li>Find by date range</li>
 *   <li>Combined filtering with multiple criteria</li>
 * </ul>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Repository
public class TransactionRepository {
    
    /**
     * Thread-safe in-memory storage for transactions.
     * Key: Transaction ID, Value: Transaction object
     */
    private final ConcurrentHashMap<String, Transaction> transactions = new ConcurrentHashMap<>();
    
    /**
     * Saves a transaction to the repository.
     * If a transaction with the same ID exists, it will be replaced.
     * 
     * @param transaction The transaction to save
     * @return The saved transaction
     * @throws IllegalArgumentException if transaction or transaction ID is null
     */
    public Transaction save(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        if (transaction.getId() == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }
        
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }
    
    /**
     * Finds a transaction by its unique identifier.
     * 
     * @param id The transaction ID to search for
     * @return An Optional containing the transaction if found, empty otherwise
     */
    public Optional<Transaction> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(transactions.get(id));
    }
    
    /**
     * Retrieves all transactions in the repository.
     * 
     * @return A list of all transactions (empty list if none exist)
     */
    public List<Transaction> findAll() {
        return transactions.values().stream()
                .collect(Collectors.toList());
    }
    
    /**
     * Finds all transactions involving a specific account.
     * A transaction involves an account if it appears as either fromAccount or toAccount.
     * 
     * @param accountId The account ID to search for
     * @return A list of transactions involving the account (empty list if none found)
     */
    public List<Transaction> findByAccount(String accountId) {
        if (accountId == null) {
            return List.of();
        }
        
        return transactions.values().stream()
                .filter(t -> t.involvesAccount(accountId))
                .collect(Collectors.toList());
    }
    
    /**
     * Finds all transactions of a specific type.
     * 
     * @param type The transaction type to filter by
     * @return A list of transactions of the specified type (empty list if none found)
     */
    public List<Transaction> findByType(TransactionType type) {
        if (type == null) {
            return List.of();
        }
        
        return transactions.values().stream()
                .filter(t -> type.equals(t.getType()))
                .collect(Collectors.toList());
    }
    
    /**
     * Finds all transactions within a date range (inclusive).
     * 
     * @param from Start date of the range (null means no lower bound)
     * @param to End date of the range (null means no upper bound)
     * @return A list of transactions within the date range (empty list if none found)
     */
    public List<Transaction> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return transactions.values().stream()
                .filter(t -> t.isWithinDateRange(from, to))
                .collect(Collectors.toList());
    }
    
    /**
     * Finds transactions matching multiple filter criteria.
     * All non-null parameters are applied as filters.
     * 
     * @param accountId Optional account ID filter (both fromAccount and toAccount checked)
     * @param type Optional transaction type filter
     * @param from Optional start date filter (null means no lower bound)
     * @param to Optional end date filter (null means no upper bound)
     * @param status Optional transaction status filter
     * @return A list of transactions matching all specified criteria
     */
    public List<Transaction> findByFilters(String accountId, TransactionType type, 
                                          LocalDateTime from, LocalDateTime to,
                                          com.banking.api.model.TransactionStatus status) {
        return transactions.values().stream()
                .filter(t -> accountId == null || t.involvesAccount(accountId))
                .filter(t -> type == null || type.equals(t.getType()))
                .filter(t -> t.isWithinDateRange(from, to))
                .filter(t -> status == null || status.equals(t.getStatus()))
                .collect(Collectors.toList());
    }
    
    /**
     * Deletes a transaction by its ID.
     * 
     * @param id The ID of the transaction to delete
     * @return true if the transaction was deleted, false if it didn't exist
     */
    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }
        return transactions.remove(id) != null;
    }
    
    /**
     * Checks if a transaction with the given ID exists.
     * 
     * @param id The transaction ID to check
     * @return true if a transaction with the ID exists, false otherwise
     */
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        return transactions.containsKey(id);
    }
    
    /**
     * Returns the total number of transactions in the repository.
     * 
     * @return The count of all transactions
     */
    public long count() {
        return transactions.size();
    }
    
    /**
     * Removes all transactions from the repository.
     * Use with caution - this operation cannot be undone.
     */
    public void deleteAll() {
        transactions.clear();
    }
}
