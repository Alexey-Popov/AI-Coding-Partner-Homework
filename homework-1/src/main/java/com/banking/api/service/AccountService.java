package com.banking.api.service;

import com.banking.api.model.Transaction;
import com.banking.api.model.TransactionStatus;
import com.banking.api.model.TransactionType;
import com.banking.api.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer for account-related operations.
 * 
 * <p>This service handles account balance calculations based on transaction history.
 * Balance is calculated dynamically from all COMPLETED transactions involving the account.
 * 
 * <p><b>Balance Calculation Rules:</b>
 * <ul>
 *   <li><b>DEPOSIT</b> to account (toAccount): Adds amount to balance</li>
 *   <li><b>WITHDRAWAL</b> from account (fromAccount): Subtracts amount from balance</li>
 *   <li><b>TRANSFER</b> from account (fromAccount): Subtracts amount from balance</li>
 *   <li><b>TRANSFER</b> to account (toAccount): Adds amount to balance</li>
 * </ul>
 * 
 * <p><b>Important Notes:</b>
 * <ul>
 *   <li>Only COMPLETED transactions are included in balance calculation</li>
 *   <li>PENDING and FAILED transactions are excluded</li>
 *   <li>Balance can be negative (overdraft allowed)</li>
 *   <li>Each transaction is processed atomically</li>
 * </ul>
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Service
public class AccountService {
    
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    
    private final TransactionRepository transactionRepository;
    
    /**
     * Constructor injection of dependencies.
     * 
     * @param transactionRepository Repository for transaction data access
     */
    public AccountService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    /**
     * Calculates the current balance for a specific account.
     * 
     * <p>The balance is computed by iterating through all COMPLETED transactions
     * that involve the account and applying the following logic:
     * <ul>
     *   <li>If account is toAccount (receiving): ADD amount</li>
     *   <li>If account is fromAccount (sending): SUBTRACT amount</li>
     * </ul>
     * 
     * <p><b>Examples:</b>
     * <pre>
     * DEPOSIT to ACC-12345 (100 USD): balance += 100
     * WITHDRAWAL from ACC-12345 (50 USD): balance -= 50
     * TRANSFER from ACC-12345 to ACC-67890 (30 USD): 
     *   - ACC-12345 balance -= 30
     *   - ACC-67890 balance += 30
     * </pre>
     * 
     * @param accountId The account identifier to calculate balance for
     * @return The calculated balance (can be negative)
     */
    public BigDecimal calculateBalance(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        
        log.debug("Calculating balance for account: {}", accountId);
        
        // Get all transactions involving this account
        List<Transaction> transactions = transactionRepository.findByAccount(accountId);
        
        BigDecimal balance = BigDecimal.ZERO;
        
        for (Transaction transaction : transactions) {
            // Only process COMPLETED transactions
            if (transaction.getStatus() != TransactionStatus.COMPLETED) {
                log.debug("Skipping non-completed transaction: id={}, status={}", 
                        transaction.getId(), transaction.getStatus());
                continue;
            }
            
            BigDecimal amount = transaction.getAmount();
            TransactionType type = transaction.getType();
            
            // Apply balance changes based on transaction type and account role
            if (type == TransactionType.DEPOSIT) {
                // DEPOSIT: money added to toAccount
                if (accountId.equals(transaction.getToAccount())) {
                    balance = balance.add(amount);
                    log.trace("DEPOSIT to account: +{} (new balance: {})", amount, balance);
                }
            } 
            else if (type == TransactionType.WITHDRAWAL) {
                // WITHDRAWAL: money removed from fromAccount
                if (accountId.equals(transaction.getFromAccount())) {
                    balance = balance.subtract(amount);
                    log.trace("WITHDRAWAL from account: -{} (new balance: {})", amount, balance);
                }
            } 
            else if (type == TransactionType.TRANSFER) {
                // TRANSFER: money moved between accounts
                if (accountId.equals(transaction.getFromAccount())) {
                    // Sending account: subtract amount
                    balance = balance.subtract(amount);
                    log.trace("TRANSFER from account: -{} (new balance: {})", amount, balance);
                } else if (accountId.equals(transaction.getToAccount())) {
                    // Receiving account: add amount
                    balance = balance.add(amount);
                    log.trace("TRANSFER to account: +{} (new balance: {})", amount, balance);
                }
            }
        }
        
        log.info("Balance calculated for account {}: {} (from {} transactions)", 
                accountId, balance, transactions.size());
        
        return balance;
    }
    
    /**
     * Retrieves the most commonly used currency for an account.
     * Used to determine the currency for balance display.
     * 
     * @param accountId The account identifier
     * @return The most common currency code, or "USD" as default
     */
    public String getAccountCurrency(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return "USD";
        }
        
        List<Transaction> transactions = transactionRepository.findByAccount(accountId);
        
        if (transactions.isEmpty()) {
            return "USD"; // Default currency when no transactions exist
        }
        
        // Return currency from the most recent transaction
        return transactions.stream()
                .filter(t -> t.getCurrency() != null)
                .findFirst()
                .map(Transaction::getCurrency)
                .orElse("USD");
    }
    
    /**
     * Checks if an account exists (has at least one transaction).
     * 
     * @param accountId The account identifier to check
     * @return true if the account has transactions, false otherwise
     */
    public boolean accountExists(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            return false;
        }
        
        List<Transaction> transactions = transactionRepository.findByAccount(accountId);
        return !transactions.isEmpty();
    }
    
    /**
     * Alias for calculateBalance method.
     * Used by InterestService for consistency.
     * 
     * @param accountId The account identifier
     * @return The calculated balance
     */
    public BigDecimal getBalance(String accountId) {
        return calculateBalance(accountId);
    }
    
    /**
     * Generates a comprehensive transaction summary for an account.
     * 
     * <p>This method calculates:
     * <ul>
     *   <li>Total deposits to the account</li>
     *   <li>Total withdrawals from the account</li>
     *   <li>Total number of transactions involving the account</li>
     *   <li>Most recent transaction timestamp</li>
     *   <li>Current balance</li>
     * </ul>
     * 
     * @param accountId The account identifier
     * @return TransactionSummary with all calculated metrics
     */
    public com.banking.api.dto.TransactionSummary getTransactionSummary(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        
        log.debug("Generating transaction summary for account: {}", accountId);
        
        List<Transaction> transactions = transactionRepository.findByAccount(accountId);
        
        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        java.time.LocalDateTime mostRecentDate = null;
        
        for (Transaction transaction : transactions) {
            // Only process COMPLETED transactions
            if (transaction.getStatus() != TransactionStatus.COMPLETED) {
                continue;
            }
            
            BigDecimal amount = transaction.getAmount();
            TransactionType type = transaction.getType();
            
            // Calculate total deposits and withdrawals
            if (type == TransactionType.DEPOSIT && accountId.equals(transaction.getToAccount())) {
                totalDeposits = totalDeposits.add(amount);
            } else if (type == TransactionType.WITHDRAWAL && accountId.equals(transaction.getFromAccount())) {
                totalWithdrawals = totalWithdrawals.add(amount);
            } else if (type == TransactionType.TRANSFER) {
                if (accountId.equals(transaction.getFromAccount())) {
                    totalWithdrawals = totalWithdrawals.add(amount);
                } else if (accountId.equals(transaction.getToAccount())) {
                    totalDeposits = totalDeposits.add(amount);
                }
            }
            
            // Track most recent transaction date
            if (transaction.getTimestamp() != null) {
                if (mostRecentDate == null || transaction.getTimestamp().isAfter(mostRecentDate)) {
                    mostRecentDate = transaction.getTimestamp();
                }
            }
        }
        
        // Calculate current balance
        BigDecimal currentBalance = calculateBalance(accountId);
        String currency = getAccountCurrency(accountId);
        
        return com.banking.api.dto.TransactionSummary.builder()
                .accountId(accountId)
                .totalDeposits(totalDeposits)
                .totalWithdrawals(totalWithdrawals)
                .numberOfTransactions(transactions.size())
                .mostRecentTransactionDate(mostRecentDate)
                .currentBalance(currentBalance)
                .currency(currency)
                .build();
    }
}
