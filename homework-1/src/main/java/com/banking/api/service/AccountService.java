package com.banking.api.service;

import com.banking.api.dto.BalanceResponse;
import com.banking.api.dto.InterestResponse;
import com.banking.api.dto.SummaryResponse;
import com.banking.api.model.Transaction;
import com.banking.api.model.TransactionType;
import com.banking.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final TransactionRepository transactionRepository;
    
    public BalanceResponse getBalance(String accountId) {
        BigDecimal balance = calculateBalance(accountId);
        return BalanceResponse.builder()
                .accountId(accountId)
                .balance(balance)
                .currency("USD")
                .build();
    }
    
    public BigDecimal calculateBalance(String accountId) {
        List<Transaction> accountTransactions = transactionRepository.findByAccountId(accountId);
        
        BigDecimal balance = BigDecimal.ZERO;
        
        for (Transaction transaction : accountTransactions) {
            if (transaction.getType() == TransactionType.DEPOSIT && 
                    accountId.equals(transaction.getToAccount())) {
                balance = balance.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.WITHDRAWAL && 
                    accountId.equals(transaction.getFromAccount())) {
                balance = balance.subtract(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.TRANSFER) {
                if (accountId.equals(transaction.getFromAccount())) {
                    balance = balance.subtract(transaction.getAmount());
                }
                if (accountId.equals(transaction.getToAccount())) {
                    balance = balance.add(transaction.getAmount());
                }
            }
        }
        
        return balance;
    }
    
    public SummaryResponse getAccountSummary(String accountId) {
        List<Transaction> accountTransactions = transactionRepository.findByAccountId(accountId);
        
        BigDecimal totalDeposits = accountTransactions.stream()
                .filter(t -> (t.getType() == TransactionType.DEPOSIT && accountId.equals(t.getToAccount())) ||
                        (t.getType() == TransactionType.TRANSFER && accountId.equals(t.getToAccount())))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalWithdrawals = accountTransactions.stream()
                .filter(t -> (t.getType() == TransactionType.WITHDRAWAL && accountId.equals(t.getFromAccount())) ||
                        (t.getType() == TransactionType.TRANSFER && accountId.equals(t.getFromAccount())))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int numberOfTransactions = accountTransactions.size();
        
        LocalDateTime mostRecentDate = accountTransactions.stream()
                .map(Transaction::getTimestamp)
                .max(Comparator.naturalOrder())
                .orElse(null);
        
        return SummaryResponse.builder()
                .accountId(accountId)
                .totalDeposits(totalDeposits)
                .totalWithdrawals(totalWithdrawals)
                .numberOfTransactions(numberOfTransactions)
                .mostRecentTransactionDate(mostRecentDate)
                .build();
    }
    
    public InterestResponse calculateInterest(String accountId, double rate, int days) {
        BigDecimal principal = calculateBalance(accountId);
        
        // Simple Interest = Principal × Rate × Time (in years)
        BigDecimal rateDecimal = BigDecimal.valueOf(rate);
        BigDecimal timeInYears = BigDecimal.valueOf(days).divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        BigDecimal interest = principal.multiply(rateDecimal).multiply(timeInYears)
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal totalAmount = principal.add(interest);
        
        return InterestResponse.builder()
                .accountId(accountId)
                .principal(principal)
                .rate(rate)
                .days(days)
                .interest(interest)
                .totalAmount(totalAmount)
                .build();
    }
}
