package com.banking.transactions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class AccountSummary {
    private int totalDeposits;
    private int totalWithdrawals;
    private int numberOfTransactions;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime mostRecentTransactionDate;

    public AccountSummary() {
    }

    public AccountSummary(int totalDeposits, 
                         int totalWithdrawals,
                         int numberOfTransactions, 
                         LocalDateTime mostRecentTransactionDate) {
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.numberOfTransactions = numberOfTransactions;
        this.mostRecentTransactionDate = mostRecentTransactionDate;
    }

    public int getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(int totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public int getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(int totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(int numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public LocalDateTime getMostRecentTransactionDate() {
        return mostRecentTransactionDate;
    }

    public void setMostRecentTransactionDate(LocalDateTime mostRecentTransactionDate) {
        this.mostRecentTransactionDate = mostRecentTransactionDate;
    }
}
