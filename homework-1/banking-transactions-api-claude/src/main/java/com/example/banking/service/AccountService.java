package com.example.banking.service;

import com.example.banking.dto.AccountBalanceResponse;
import com.example.banking.dto.AccountSummaryResponse;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionStatus;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {

    private final TransactionRepository transactionRepository;

    public AccountService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public AccountBalanceResponse getAccountBalance(String accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        Map<String, BigDecimal> balances = new HashMap<>();

        for (Transaction transaction : transactions) {
            // Only process completed transactions
            if (transaction.getStatus() != TransactionStatus.COMPLETED) {
                continue;
            }

            String currency = transaction.getCurrency();
            BigDecimal currentBalance = balances.getOrDefault(currency, BigDecimal.ZERO);

            // Calculate balance based on transaction type and account involvement
            if (transaction.getType() == TransactionType.DEPOSIT && accountId.equals(transaction.getToAccount())) {
                // Money coming in
                currentBalance = currentBalance.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.WITHDRAWAL && accountId.equals(transaction.getFromAccount())) {
                // Money going out
                currentBalance = currentBalance.subtract(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.TRANSFER) {
                if (accountId.equals(transaction.getFromAccount())) {
                    // Money going out
                    currentBalance = currentBalance.subtract(transaction.getAmount());
                } else if (accountId.equals(transaction.getToAccount())) {
                    // Money coming in
                    currentBalance = currentBalance.add(transaction.getAmount());
                }
            }

            balances.put(currency, currentBalance);
        }

        return new AccountBalanceResponse(accountId, balances);
    }

    public AccountSummaryResponse getAccountSummary(String accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        Map<String, BigDecimal> totalDeposits = new HashMap<>();
        Map<String, BigDecimal> totalWithdrawals = new HashMap<>();
        int numberOfTransactions = transactions.size();
        Instant mostRecentDate = null;

        for (Transaction transaction : transactions) {
            String currency = transaction.getCurrency();

            // Track deposits (money coming in)
            if ((transaction.getType() == TransactionType.DEPOSIT && accountId.equals(transaction.getToAccount())) ||
                (transaction.getType() == TransactionType.TRANSFER && accountId.equals(transaction.getToAccount()))) {
                BigDecimal currentDeposits = totalDeposits.getOrDefault(currency, BigDecimal.ZERO);
                totalDeposits.put(currency, currentDeposits.add(transaction.getAmount()));
            }

            // Track withdrawals (money going out)
            if ((transaction.getType() == TransactionType.WITHDRAWAL && accountId.equals(transaction.getFromAccount())) ||
                (transaction.getType() == TransactionType.TRANSFER && accountId.equals(transaction.getFromAccount()))) {
                BigDecimal currentWithdrawals = totalWithdrawals.getOrDefault(currency, BigDecimal.ZERO);
                totalWithdrawals.put(currency, currentWithdrawals.add(transaction.getAmount()));
            }

            // Find most recent transaction date
            if (mostRecentDate == null || transaction.getTimestamp().isAfter(mostRecentDate)) {
                mostRecentDate = transaction.getTimestamp();
            }
        }

        return new AccountSummaryResponse(accountId, totalDeposits, totalWithdrawals,
                numberOfTransactions, mostRecentDate);
    }
}
