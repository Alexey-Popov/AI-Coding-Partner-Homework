package com.banking.transactions.controller;

import com.banking.transactions.dto.AccountBalanceResponse;
import com.banking.transactions.dto.AccountSummaryResponse;
import com.banking.transactions.model.Transaction;
import com.banking.transactions.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping
public class TransactionController {
    
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * POST /transactions - Create a new transaction
     */
    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    /**
     * GET /transactions - List all transactions with optional filters
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<Transaction> transactions = transactionService.getTransactions(accountId, type, from, to);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /transactions/:id - Get a specific transaction by ID
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /accounts/:accountId/balance - Get account balance
     */
    @GetMapping("/accounts/{accountId}/balance")
    public ResponseEntity<AccountBalanceResponse> getAccountBalance(@PathVariable String accountId) {
        AccountBalanceResponse balance = transactionService.getAccountBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    /**
     * GET /accounts/:accountId/summary - Get account summary
     */
    @GetMapping("/accounts/{accountId}/summary")
    public ResponseEntity<?> getAccountSummary(@PathVariable String accountId) {
        AccountSummaryResponse summary = transactionService.getAccountSummary(accountId);
        if (summary == null) {
            return ResponseEntity.status(404).body("No transactions found");
        }
        return ResponseEntity.ok(summary);
    }
}
