package com.example.demo.model;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountSummary {
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private int transactionCount;
    private Instant mostRecentTransactionDate;

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(BigDecimal totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(BigDecimal totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Instant getMostRecentTransactionDate() {
        return mostRecentTransactionDate;
    }

    public void setMostRecentTransactionDate(Instant mostRecentTransactionDate) {
        this.mostRecentTransactionDate = mostRecentTransactionDate;
    }
}
