package com.example.banking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class AccountSummaryResponse {

    private String accountId;
    private Map<String, BigDecimal> totalDeposits;
    private Map<String, BigDecimal> totalWithdrawals;
    private int numberOfTransactions;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant mostRecentTransactionDate;

    public AccountSummaryResponse() {
    }

    public AccountSummaryResponse(String accountId, Map<String, BigDecimal> totalDeposits,
                                   Map<String, BigDecimal> totalWithdrawals, int numberOfTransactions,
                                   Instant mostRecentTransactionDate) {
        this.accountId = accountId;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.numberOfTransactions = numberOfTransactions;
        this.mostRecentTransactionDate = mostRecentTransactionDate;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Map<String, BigDecimal> getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(Map<String, BigDecimal> totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public Map<String, BigDecimal> getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(Map<String, BigDecimal> totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(int numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public Instant getMostRecentTransactionDate() {
        return mostRecentTransactionDate;
    }

    public void setMostRecentTransactionDate(Instant mostRecentTransactionDate) {
        this.mostRecentTransactionDate = mostRecentTransactionDate;
    }
}
