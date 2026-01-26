package com.banking.transactions.controller;

import com.banking.transactions.dto.AccountSummary;
import com.banking.transactions.dto.BalanceResponse;
import com.banking.transactions.service.AccountService;
import com.banking.transactions.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing account-related operations.
 * Provides endpoints for account balance and transaction summaries.
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    /**
     * Constructs a new AccountController.
     *
     * @param accountService the service for account operations
     * @param transactionService the service for transaction operations
     */
    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    /**
     * Retrieves the balance for a specific account.
     * Returns balance per currency for multi-currency support.
     *
     * @param accountId the unique identifier of the account
     * @return ResponseEntity with balance information and HTTP 200 status
     */
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getAccountBalance(@PathVariable String accountId) {
        try {
            BalanceResponse balance = accountService.getAccountBalance(accountId);
            return new ResponseEntity<>(balance, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a statistical summary for a specific account.
     * Includes deposit count, withdrawal count, total transactions, and most recent transaction date.
     *
     * @param accountId the unique identifier of the account
     * @return ResponseEntity with account summary and HTTP 200 status
     */
    @GetMapping("/{accountId}/summary")
    public ResponseEntity<AccountSummary> getAccountSummary(@PathVariable String accountId) {
        AccountSummary summary = transactionService.getAccountSummary(accountId);
        return ResponseEntity.ok(summary);
    }
}
