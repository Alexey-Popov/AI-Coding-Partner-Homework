package com.banking.transactions.controller;

import com.banking.transactions.model.Transaction;
import com.banking.transactions.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing banking transactions.
 * Provides endpoints for creating, retrieving, and filtering transactions.
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Constructs a new TransactionController.
     *
     * @param transactionService the service for transaction operations
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Creates a new transaction with validation.
     *
     * @param transaction the transaction to create
     * @return ResponseEntity with the created transaction and HTTP 201 status
     * @throws com.banking.transactions.exception.ValidationException if validation fails
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.createTransaction(transaction);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    /**
     * Retrieves all transactions with optional filtering.
     *
     * @param accountId optional account ID filter (matches fromAccount or toAccount)
     * @param type optional transaction type filter (deposit, withdrawal, transfer)
     * @param from optional start date filter (ISO 8601 format: YYYY-MM-DD)
     * @param to optional end date filter (ISO 8601 format: YYYY-MM-DD)
     * @return ResponseEntity with list of transactions and HTTP 200 status
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        
        // If any filter is provided, use filtered results; otherwise return all
        List<Transaction> transactions;
        if (accountId != null || type != null || from != null || to != null) {
            transactions = transactionService.getFilteredTransactions(accountId, type, from, to);
        } else {
            transactions = transactionService.getAllTransactions();
        }
        
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    /**
     * Retrieves a specific transaction by its ID.
     *
     * @param id the unique identifier of the transaction
     * @return ResponseEntity with the transaction and HTTP 200, or HTTP 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String id) {
        Transaction transaction = transactionService.getTransactionById(id);
        if (transaction != null) {
            return new ResponseEntity<>(transaction, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
