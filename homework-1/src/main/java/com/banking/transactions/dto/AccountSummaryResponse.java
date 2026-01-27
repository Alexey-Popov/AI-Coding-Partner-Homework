package com.banking.transactions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountSummaryResponse {
    
    private String accountId;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private Integer numberOfTransactions;
    private LocalDateTime mostRecentTransactionDate;

    public AccountSummaryResponse() {
    }

    // Constructor for when transactions exist
    public AccountSummaryResponse(String accountId, BigDecimal totalDeposits,
                                 BigDecimal totalWithdrawals, Integer numberOfTransactions, 
                                 LocalDateTime mostRecentTransactionDate) {
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

    public Integer getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(Integer numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public LocalDateTime getMostRecentTransactionDate() {
        return mostRecentTransactionDate;
    }

    public void setMostRecentTransactionDate(LocalDateTime mostRecentTransactionDate) {
        this.mostRecentTransactionDate = mostRecentTransactionDate;
    }

    @Override
    public String toString() {
        return "AccountSummaryResponse{" +
                "accountId='" + accountId + '\'' +
                ", totalDeposits=" + totalDeposits +
                ", totalWithdrawals=" + totalWithdrawals +
                ", numberOfTransactions=" + numberOfTransactions +
                ", mostRecentTransactionDate=" + mostRecentTransactionDate +
                '}';
    }
}
