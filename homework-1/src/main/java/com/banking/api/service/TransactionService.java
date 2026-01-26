package com.banking.api.service;

import com.banking.api.dto.TransactionRequest;
import com.banking.api.model.Transaction;
import com.banking.api.model.TransactionStatus;
import com.banking.api.model.TransactionType;
import com.banking.api.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for transaction business logic.
 * 
 * <p>This service handles the core transaction operations including:
 * <ul>
 *   <li>Creating new transactions with auto-generated IDs and timestamps</li>
 *   <li>Retrieving transactions by ID or with filters</li>
 *   <li>Setting transaction status (default: COMPLETED)</li>
 *   <li>Coordinating with repository for data persistence</li>
 * </ul>
 * 
 * <p><b>Transaction Creation Process:</b>
 * <ol>
 *   <li>Accept TransactionRequest from controller</li>
 *   <li>Generate unique UUID for transaction ID</li>
 *   <li>Set timestamp to current LocalDateTime</li>
 *   <li>Set status to COMPLETED (simple implementation)</li>
 *   <li>Convert type string to TransactionType enum</li>
 *   <li>Save to repository and return Transaction entity</li>
 * </ol>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Service
public class TransactionService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    
    /**
     * Constructor injection of dependencies.
     * 
     * @param transactionRepository Repository for transaction data access
     */
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    /**
     * Creates a new transaction from the provided request.
     * 
     * <p>This method:
     * <ul>
     *   <li>Generates a unique UUID for the transaction ID</li>
     *   <li>Sets the timestamp to the current date and time</li>
     *   <li>Sets the status to COMPLETED (for this simple implementation)</li>
     *   <li>Converts the type string to TransactionType enum</li>
     *   <li>Persists the transaction to the repository</li>
     * </ul>
     * 
     * @param request The transaction creation request
     * @return The created Transaction entity with generated ID, timestamp, and status
     * @throws IllegalArgumentException if request is null or type is invalid
     */
    public Transaction createTransaction(TransactionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Transaction request cannot be null");
        }
        
        log.info("Creating transaction: type={}, amount={}, currency={}", 
                request.getType(), request.getAmount(), request.getCurrency());
        
        // Convert type string to enum (case-insensitive)
        TransactionType type;
        try {
            type = TransactionType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid transaction type: {}", request.getType());
            throw new IllegalArgumentException("Invalid transaction type: " + request.getType() 
                    + ". Must be one of: deposit, withdrawal, transfer");
        }
        
        // Validate business rules based on transaction type
        validateBusinessRules(request, type);
        
        // Build transaction with auto-generated fields
        Transaction transaction = Transaction.builder()
                .id(Transaction.generateId())
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .type(type)
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
        
        // Save to repository
        Transaction saved = transactionRepository.save(transaction);
        
        log.info("Transaction created successfully: id={}, status={}", 
                saved.getId(), saved.getStatus());
        
        return saved;
    }
    
    /**
     * Validates business rules for transaction requests.
     * 
     * <p><b>Business Rules:</b>
     * <ul>
     *   <li>TRANSFER: Both fromAccount and toAccount required, must be different</li>
     *   <li>DEPOSIT: toAccount required, fromAccount must be null</li>
     *   <li>WITHDRAWAL: fromAccount required, toAccount must be null</li>
     * </ul>
     * 
     * @param request The transaction request to validate
     * @param type The transaction type enum
     * @throws com.banking.api.exception.ValidationException if business rules are violated
     */
    private void validateBusinessRules(TransactionRequest request, TransactionType type) {
        java.util.List<com.banking.api.dto.ValidationError> errors = new java.util.ArrayList<>();
        
        switch (type) {
            case TRANSFER:
                if (request.getFromAccount() == null || request.getFromAccount().trim().isEmpty()) {
                    errors.add(com.banking.api.dto.ValidationError.builder()
                            .field("fromAccount")
                            .message("From account is required for transfer transactions")
                            .build());
                }
                if (request.getToAccount() == null || request.getToAccount().trim().isEmpty()) {
                    errors.add(com.banking.api.dto.ValidationError.builder()
                            .field("toAccount")
                            .message("To account is required for transfer transactions")
                            .build());
                }
                if (request.getFromAccount() != null && request.getToAccount() != null 
                        && request.getFromAccount().equals(request.getToAccount())) {
                    errors.add(com.banking.api.dto.ValidationError.builder()
                            .field("toAccount")
                            .message("From account and to account must be different for transfers")
                            .build());
                }
                break;
                
            case DEPOSIT:
                if (request.getToAccount() == null || request.getToAccount().trim().isEmpty()) {
                    errors.add(com.banking.api.dto.ValidationError.builder()
                            .field("toAccount")
                            .message("To account is required for deposit transactions")
                            .build());
                }
                if (request.getFromAccount() != null && !request.getFromAccount().trim().isEmpty()) {
                    errors.add(com.banking.api.dto.ValidationError.builder()
                            .field("fromAccount")
                            .message("From account should not be specified for deposit transactions")
                            .build());
                }
                break;
                
            case WITHDRAWAL:
                if (request.getFromAccount() == null || request.getFromAccount().trim().isEmpty()) {
                    errors.add(com.banking.api.dto.ValidationError.builder()
                            .field("fromAccount")
                            .message("From account is required for withdrawal transactions")
                            .build());
                }
                if (request.getToAccount() != null && !request.getToAccount().trim().isEmpty()) {
                    errors.add(com.banking.api.dto.ValidationError.builder()
                            .field("toAccount")
                            .message("To account should not be specified for withdrawal transactions")
                            .build());
                }
                break;
        }
        
        if (!errors.isEmpty()) {
            log.warn("Business rule validation failed for {} transaction with {} errors", type, errors.size());
            throw new com.banking.api.exception.ValidationException(
                    "Business rule validation failed", errors);
        }
    }
    
    /**
     * Retrieves a transaction by its unique identifier.
     * 
     * @param id The transaction ID to search for
     * @return Optional containing the transaction if found, empty otherwise
     */
    public Optional<Transaction> getTransactionById(String id) {
        log.debug("Fetching transaction by id: {}", id);
        return transactionRepository.findById(id);
    }
    
    /**
     * Retrieves all transactions in the system.
     * 
     * @return List of all transactions (empty list if none exist)
     */
    public List<Transaction> getAllTransactions() {
        log.debug("Fetching all transactions");
        return transactionRepository.findAll();
    }
    
    /**
     * Retrieves transactions filtered by account ID.
     * A transaction matches if it involves the account as either source or destination.
     * 
     * @param accountId The account ID to filter by
     * @return List of transactions involving the account
     */
    public List<Transaction> getTransactionsByAccount(String accountId) {
        log.debug("Fetching transactions for account: {}", accountId);
        return transactionRepository.findByAccount(accountId);
    }
    
    /**
     * Retrieves transactions filtered by type.
     * 
     * @param type The transaction type to filter by
     * @return List of transactions of the specified type
     */
    public List<Transaction> getTransactionsByType(TransactionType type) {
        log.debug("Fetching transactions by type: {}", type);
        return transactionRepository.findByType(type);
    }
    
    /**
     * Retrieves transactions within a date range.
     * 
     * @param from Start date (inclusive, null means no lower bound)
     * @param to End date (inclusive, null means no upper bound)
     * @return List of transactions within the date range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDateTime from, LocalDateTime to) {
        log.debug("Fetching transactions in date range: from={}, to={}", from, to);
        return transactionRepository.findByDateRange(from, to);
    }
    
    /**
     * Retrieves transactions matching multiple filter criteria.
     * All non-null parameters are applied as filters.
     * 
     * @param accountId Optional account ID filter
     * @param type Optional transaction type filter
     * @param from Optional start date filter
     * @param to Optional end date filter
     * @param status Optional transaction status filter
     * @return List of transactions matching all specified criteria
     */
    public List<Transaction> getTransactionsWithFilters(String accountId, TransactionType type,
                                                        LocalDateTime from, LocalDateTime to,
                                                        TransactionStatus status) {
        log.debug("Fetching transactions with filters: account={}, type={}, from={}, to={}, status={}", 
                accountId, type, from, to, status);
        return transactionRepository.findByFilters(accountId, type, from, to, status);
    }
    
    /**
     * Returns the total count of transactions in the system.
     * 
     * @return Total number of transactions
     */
    public long getTransactionCount() {
        return transactionRepository.count();
    }
    
    /**
     * Filters transactions and returns Transaction models (not DTOs).
     * Used by ExportController for CSV generation.
     * 
     * @param accountId Optional account ID filter
     * @param type Optional transaction type filter (as string)
     * @param from Optional start date filter (as string)
     * @param to Optional end date filter (as string)
     * @param status Optional status filter (as string)
     * @return List of Transaction models matching filters
     */
    public List<Transaction> filterTransactionsAsModels(String accountId, String type, 
                                                        String from, String to, String status) {
        // Parse type
        TransactionType transactionType = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                transactionType = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid transaction type filter: {}", type);
            }
        }
        
        // Parse dates
        java.time.LocalDateTime fromDate = null;
        java.time.LocalDateTime toDate = null;
        
        if (from != null && !from.trim().isEmpty()) {
            try {
                fromDate = java.time.LocalDateTime.parse(from);
            } catch (Exception e) {
                log.warn("Invalid from date: {}", from);
            }
        }
        
        if (to != null && !to.trim().isEmpty()) {
            try {
                toDate = java.time.LocalDateTime.parse(to);
            } catch (Exception e) {
                log.warn("Invalid to date: {}", to);
            }
        }
        
        // Parse status
        com.banking.api.model.TransactionStatus transactionStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                transactionStatus = com.banking.api.model.TransactionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid transaction status filter: {}", status);
            }
        }
        
        // Apply filters
        if (accountId != null || transactionType != null || fromDate != null || toDate != null || transactionStatus != null) {
            return transactionRepository.findByFilters(accountId, transactionType, fromDate, toDate, transactionStatus);
        } else {
            return transactionRepository.findAll();
        }
    }
}
