package com.banking.api.controller;

import com.banking.api.dto.TransactionRequest;
import com.banking.api.dto.TransactionResponse;
import com.banking.api.model.Transaction;
import com.banking.api.model.TransactionType;
import com.banking.api.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for transaction endpoints.
 * 
 * <p>This controller handles all transaction-related HTTP requests including:
 * <ul>
 *   <li>POST /api/v1/transactions - Create a new transaction</li>
 *   <li>GET /api/v1/transactions - List all transactions with optional filters</li>
 *   <li>GET /api/v1/transactions/{id} - Get a specific transaction by ID</li>
 * </ul>
 * 
 * <p><b>Base Path:</b> All endpoints are prefixed with /api/v1 as configured
 * in application.properties (server.servlet.context-path=/api/v1)
 * 
 * <p><b>Response Status Codes:</b>
 * <ul>
 *   <li>200 OK - Successful GET requests</li>
 *   <li>201 Created - Successful POST requests</li>
 *   <li>400 Bad Request - Validation errors</li>
 *   <li>404 Not Found - Transaction not found</li>
 *   <li>500 Internal Server Error - Unexpected errors</li>
 * </ul>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    
    /**
     * Constructor injection of service dependencies.
     * 
     * @param transactionService Service for transaction business logic
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    /**
     * Creates a new transaction.
     * 
     * <p><b>Endpoint:</b> POST /api/v1/transactions
     * 
     * <p><b>Request Body Example:</b>
     * <pre>
     * {
     *   "fromAccount": "ACC-12345",
     *   "toAccount": "ACC-67890",
     *   "amount": 100.50,
     *   "currency": "USD",
     *   "type": "transfer"
     * }
     * </pre>
     * 
     * <p><b>Response Example (201 Created):</b>
     * <pre>
     * {
     *   "id": "550e8400-e29b-41d4-a716-446655440000",
     *   "fromAccount": "ACC-12345",
     *   "toAccount": "ACC-67890",
     *   "amount": 100.50,
     *   "currency": "USD",
     *   "type": "TRANSFER",
     *   "timestamp": "2026-01-22T10:30:00",
     *   "status": "COMPLETED"
     * }
     * </pre>
     * 
     * @param request The transaction creation request
     * @return ResponseEntity with created transaction and HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        log.info("POST /transactions - Creating new transaction: type={}, amount={}", 
                request.getType(), request.getAmount());
        
        Transaction transaction = transactionService.createTransaction(request);
        TransactionResponse response = TransactionResponse.fromEntity(transaction);
        
        log.info("Transaction created successfully: id={}", transaction.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Retrieves all transactions with optional filtering.
     * 
     * <p><b>Endpoint:</b> GET /api/v1/transactions
     * 
     * <p><b>Query Parameters (all optional):</b>
     * <ul>
     *   <li>accountId - Filter by account (matches fromAccount or toAccount)</li>
     *   <li>type - Filter by transaction type (deposit, withdrawal, transfer)</li>
     *   <li>from - Start date in ISO 8601 format (e.g., 2026-01-01T00:00:00)</li>
     *   <li>to - End date in ISO 8601 format (e.g., 2026-01-31T23:59:59)</li>
     *   <li>status - Filter by transaction status (pending, completed, failed)</li>
     * </ul>
     * 
     * <p><b>Example Requests:</b>
     * <pre>
     * GET /api/v1/transactions
     * GET /api/v1/transactions?accountId=ACC-12345
     * GET /api/v1/transactions?type=transfer
     * GET /api/v1/transactions?from=2026-01-01T00:00:00&to=2026-01-31T23:59:59
     * GET /api/v1/transactions?accountId=ACC-12345&type=transfer&from=2026-01-01T00:00:00
     * GET /api/v1/transactions?status=completed
     * GET /api/v1/transactions?accountId=ACC-12345&status=pending&type=transfer
     * </pre>
     * 
     * @param accountId Optional account ID filter
     * @param typeStr Optional transaction type filter
     * @param from Optional start date filter
     * @param to Optional end date filter
     * @param status Optional transaction status filter
     * @return ResponseEntity with list of transactions and HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String status) {
        
        log.info("GET /transactions - Filters: accountId={}, type={}, from={}, to={}, status={}", 
                accountId, type, from, to, status);
        
        List<Transaction> transactions;
        
        // Check if any filters are provided
        if (accountId != null || type != null || from != null || to != null || status != null) {
            // Convert type string to enum if provided
            TransactionType transactionType = null;
            if (type != null) {
                try {
                    transactionType = TransactionType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid transaction type filter: {}", type);
                    // Return empty list for invalid type instead of error
                    return ResponseEntity.ok(List.of());
                }
            }
            
            // Convert status string to enum if provided
            com.banking.api.model.TransactionStatus transactionStatus = null;
            if (status != null) {
                try {
                    transactionStatus = com.banking.api.model.TransactionStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid transaction status filter: {}", status);
                    // Return empty list for invalid status instead of error
                    return ResponseEntity.ok(List.of());
                }
            }
            
            // Apply filters
            transactions = transactionService.getTransactionsWithFilters(
                    accountId, transactionType, from, to, transactionStatus);
            
            log.info("Found {} transactions matching filters", transactions.size());
        } else {
            // No filters - return all transactions
            transactions = transactionService.getAllTransactions();
            log.info("Found {} total transactions", transactions.size());
        }
        
        // Convert entities to DTOs
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Retrieves a specific transaction by its unique identifier.
     * 
     * <p><b>Endpoint:</b> GET /api/v1/transactions/{id}
     * 
     * <p><b>Example Request:</b>
     * <pre>
     * GET /api/v1/transactions/550e8400-e29b-41d4-a716-446655440000
     * </pre>
     * 
     * <p><b>Response Example (200 OK):</b>
     * <pre>
     * {
     *   "id": "550e8400-e29b-41d4-a716-446655440000",
     *   "fromAccount": "ACC-12345",
     *   "toAccount": "ACC-67890",
     *   "amount": 100.50,
     *   "currency": "USD",
     *   "type": "TRANSFER",
     *   "timestamp": "2026-01-22T10:30:00",
     *   "status": "COMPLETED"
     * }
     * </pre>
     * 
     * @param id The transaction ID to retrieve
     * @return ResponseEntity with transaction and HTTP 200 status, or HTTP 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable String id) {
        log.info("GET /transactions/{} - Fetching transaction", id);
        
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        
        if (transaction.isPresent()) {
            TransactionResponse response = TransactionResponse.fromEntity(transaction.get());
            log.info("Transaction found: id={}", id);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Transaction not found: id={}", id);
            throw new com.banking.api.exception.ResourceNotFoundException(
                "Transaction not found with ID: " + id
            );
        }
    }
}
