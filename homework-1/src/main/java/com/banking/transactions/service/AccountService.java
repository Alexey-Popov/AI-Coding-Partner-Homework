package com.banking.transactions.service;

import com.banking.transactions.dto.BalanceResponse;
import com.banking.transactions.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for managing account-related operations.
 * Handles account balance calculations across multiple currencies.
 */
@Service
public class AccountService {

    private final TransactionService transactionService;

    /**
     * Constructs a new AccountService.
     *
     * @param transactionService the service for transaction operations
     */
    public AccountService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Calculates the balance for a specific account across all currencies.
     * Only includes completed transactions in the calculation.
     *
     * @param accountId the account ID to calculate balance for
     * @return BalanceResponse containing balance per currency
     */
    public BalanceResponse getAccountBalance(String accountId) {
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        
        // Map to store balance per currency
        Map<String, BigDecimal> currencyBalances = new HashMap<>();
        
        for (Transaction transaction : allTransactions) {
            // Skip failed and pending transactions
            if (transaction.getStatus() == Transaction.TransactionStatus.FAILED || 
                transaction.getStatus() == Transaction.TransactionStatus.PENDING) {
                continue;
            }
            
            boolean isFromAccount = accountId.equals(transaction.getFromAccount());
            boolean isToAccount = accountId.equals(transaction.getToAccount());
            
            if (isFromAccount || isToAccount) {
                String currency = transaction.getCurrency();
                BigDecimal currentBalance = currencyBalances.getOrDefault(currency, BigDecimal.ZERO);
                
                switch (transaction.getType()) {
                    case DEPOSIT:
                        if (isToAccount) {
                            currentBalance = currentBalance.add(transaction.getAmount());
                        }
                        break;
                    case WITHDRAWAL:
                        if (isFromAccount) {
                            currentBalance = currentBalance.subtract(transaction.getAmount());
                        }
                        break;
                    case TRANSFER:
                        if (isFromAccount) {
                            currentBalance = currentBalance.subtract(transaction.getAmount());
                        }
                        if (isToAccount) {
                            currentBalance = currentBalance.add(transaction.getAmount());
                        }
                        break;
                }
                
                currencyBalances.put(currency, currentBalance);
            }
        }
        
        // Convert map to list of CurrencyBalance objects
        List<BalanceResponse.CurrencyBalance> balances = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : currencyBalances.entrySet()) {
            balances.add(new BalanceResponse.CurrencyBalance(entry.getKey(), entry.getValue()));
        }
        
        return new BalanceResponse(accountId, balances);
    }
}
