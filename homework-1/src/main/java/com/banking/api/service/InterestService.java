package com.banking.api.service;

import com.banking.api.dto.InterestCalculation;
import com.banking.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for calculating interest on account balances
 */
@Service
@RequiredArgsConstructor
public class InterestService {

    private final AccountService accountService;

    /**
     * Calculate simple interest for an account
     * Formula: Interest = Principal × Rate × (Days/365)
     *
     * @param accountId Account ID
     * @param rate Annual interest rate (as decimal, e.g., 0.05 for 5%)
     * @param days Number of days
     * @return Interest calculation details
     */
    public InterestCalculation calculateSimpleInterest(String accountId, BigDecimal rate, int days) {
        // Validate inputs
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Interest rate must be non-negative");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }

        // Get current balance
        BigDecimal currentBalance = accountService.getBalance(accountId);
        
        // If balance is zero or negative, no interest
        if (currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return InterestCalculation.builder()
                    .accountId(accountId)
                    .currentBalance(currentBalance)
                    .interestRate(rate)
                    .days(days)
                    .interestAmount(BigDecimal.ZERO)
                    .projectedBalance(currentBalance)
                    .formula("Principal × Rate × (Days/365)")
                    .currency("USD")
                    .build();
        }

        // Calculate interest: Principal × Rate × (Days/365)
        BigDecimal daysDecimal = new BigDecimal(days);
        BigDecimal daysInYear = new BigDecimal(365);
        
        BigDecimal interestAmount = currentBalance
                .multiply(rate)
                .multiply(daysDecimal)
                .divide(daysInYear, 2, RoundingMode.HALF_UP);

        // Calculate projected balance
        BigDecimal projectedBalance = currentBalance.add(interestAmount);

        return InterestCalculation.builder()
                .accountId(accountId)
                .currentBalance(currentBalance)
                .interestRate(rate)
                .days(days)
                .interestAmount(interestAmount)
                .projectedBalance(projectedBalance)
                .formula("Principal × Rate × (Days/365)")
                .currency("USD")
                .build();
    }
}
