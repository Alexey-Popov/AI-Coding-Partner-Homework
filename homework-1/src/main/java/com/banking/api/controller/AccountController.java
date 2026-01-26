package com.banking.api.controller;

import com.banking.api.dto.BalanceResponse;
import com.banking.api.dto.InterestCalculation;
import com.banking.api.dto.TransactionSummary;
import com.banking.api.service.AccountService;
import com.banking.api.service.InterestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * REST Controller for account-related endpoints.
 * 
 * <p>This controller handles all account-related HTTP requests including:
 * <ul>
 *   <li>GET /api/v1/accounts/{accountId}/balance - Get account balance</li>
 * </ul>
 * 
 * <p><b>Base Path:</b> All endpoints are prefixed with /api/v1 as configured
 * in application.properties (server.servlet.context-path=/api/v1)
 * 
 * <p><b>Balance Calculation:</b>
 * The balance is calculated dynamically from all COMPLETED transactions
 * involving the account. The calculation includes:
 * <ul>
 *   <li>Deposits to the account (adds to balance)</li>
 *   <li>Withdrawals from the account (subtracts from balance)</li>
 *   <li>Transfers from the account (subtracts from balance)</li>
 *   <li>Transfers to the account (adds to balance)</li>
 * </ul>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {
    
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    
    private final AccountService accountService;
    private final InterestService interestService;
    
    /**
     * Constructor injection of service dependencies.
     * 
     * @param accountService Service for account-related operations
     * @param interestService Service for interest calculations
     */
    public AccountController(AccountService accountService, InterestService interestService) {
        this.accountService = accountService;
        this.interestService = interestService;
    }
    
    /**
     * Retrieves the current balance for a specific account.
     * 
     * <p><b>Endpoint:</b> GET /api/v1/accounts/{accountId}/balance
     * 
     * <p><b>Path Parameter:</b>
     * <ul>
     *   <li>accountId - The account identifier (format: ACC-XXXXX)</li>
     * </ul>
     * 
     * <p><b>Example Request:</b>
     * <pre>
     * GET /api/v1/accounts/ACC-12345/balance
     * </pre>
     * 
     * <p><b>Response Example (200 OK):</b>
     * <pre>
     * {
     *   "accountId": "ACC-12345",
     *   "balance": 5420.75,
     *   "currency": "USD",
     *   "calculatedAt": "2026-01-22T10:30:00"
     * }
     * </pre>
     * 
     * <p><b>Response for New Account (200 OK):</b>
     * <pre>
     * {
     *   "accountId": "ACC-99999",
     *   "balance": 0.00,
     *   "currency": "USD",
     *   "calculatedAt": "2026-01-22T10:30:00"
     * }
     * </pre>
     * 
     * <p><b>Note:</b> This implementation returns a zero balance for accounts
     * without transactions rather than returning 404. This allows checking
     * the balance of new accounts before any transactions are made.
     * 
     * @param accountId The account identifier
     * @return ResponseEntity with balance information and HTTP 200 status
     */
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getAccountBalance(@PathVariable String accountId) {
        log.info("GET /accounts/{}/balance - Calculating balance", accountId);
        
        // Calculate balance (returns 0 for accounts without transactions)
        BigDecimal balance = accountService.calculateBalance(accountId);
        
        // Get currency (returns default "USD" for new accounts)
        String currency = accountService.getAccountCurrency(accountId);
        
        // Build response
        BalanceResponse response = BalanceResponse.builder()
                .accountId(accountId)
                .balance(balance)
                .currency(currency)
                .calculatedAt(LocalDateTime.now())
                .build();
        
        log.info("Balance calculated for account {}: {} {}", accountId, balance, currency);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves transaction summary for a specific account.
     * 
     * <p><b>Endpoint:</b> GET /api/v1/accounts/{accountId}/summary
     * 
     * <p><b>Example Request:</b>
     * <pre>
     * GET /api/v1/accounts/ACC-12345/summary
     * </pre>
     * 
     * <p><b>Response Example (200 OK):</b>
     * <pre>
     * {
     *   "accountId": "ACC-12345",
     *   "totalDeposits": 5000.00,
     *   "totalWithdrawals": 1500.00,
     *   "numberOfTransactions": 25,
     *   "mostRecentTransactionDate": "2026-01-22T15:30:00",
     *   "currentBalance": 3500.00,
     *   "currency": "USD"
     * }
     * </pre>
     * 
     * @param accountId The account identifier
     * @return ResponseEntity with transaction summary and HTTP 200 status
     */
    @GetMapping("/{accountId}/summary")
    public ResponseEntity<TransactionSummary> getAccountSummary(@PathVariable String accountId) {
        log.info("GET /accounts/{}/summary - Generating transaction summary", accountId);
        
        TransactionSummary summary = accountService.getTransactionSummary(accountId);
        
        log.info("Summary generated for account {}: {} transactions, balance: {}", 
                accountId, summary.getNumberOfTransactions(), summary.getCurrentBalance());
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Calculates simple interest on account balance.
     * 
     * <p><b>Endpoint:</b> GET /api/v1/accounts/{accountId}/interest
     * 
     * <p><b>Query Parameters:</b>
     * <ul>
     *   <li>rate - Annual interest rate as decimal (e.g., 0.05 for 5%)</li>
     *   <li>days - Number of days for calculation</li>
     * </ul>
     * 
     * <p><b>Example Request:</b>
     * <pre>
     * GET /api/v1/accounts/ACC-12345/interest?rate=0.05&days=30
     * </pre>
     * 
     * <p><b>Response Example (200 OK):</b>
     * <pre>
     * {
     *   "accountId": "ACC-12345",
     *   "currentBalance": 10000.00,
     *   "interestRate": 0.05,
     *   "days": 30,
     *   "interestAmount": 41.10,
     *   "projectedBalance": 10041.10,
     *   "formula": "Principal × Rate × (Days/365)",
     *   "currency": "USD"
     * }
     * </pre>
     * 
     * @param accountId The account identifier
     * @param rate Annual interest rate as decimal
     * @param days Number of days
     * @return ResponseEntity with interest calculation and HTTP 200 status
     */
    @GetMapping("/{accountId}/interest")
    public ResponseEntity<InterestCalculation> calculateInterest(
            @PathVariable String accountId,
            @RequestParam java.math.BigDecimal rate,
            @RequestParam int days) {
        
        log.info("GET /accounts/{}/interest - Calculating interest: rate={}, days={}", 
                accountId, rate, days);
        
        InterestCalculation calculation = interestService.calculateSimpleInterest(accountId, rate, days);
        
        log.info("Interest calculated for account {}: {} over {} days = {}", 
                accountId, rate, days, calculation.getInterestAmount());
        
        return ResponseEntity.ok(calculation);
    }
}
