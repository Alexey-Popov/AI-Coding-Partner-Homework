package com.example.banking.dto;

import java.math.BigDecimal;
import java.util.Map;

public class AccountBalanceResponse {

    private String accountId;
    private Map<String, BigDecimal> balances;

    public AccountBalanceResponse() {
    }

    public AccountBalanceResponse(String accountId, Map<String, BigDecimal> balances) {
        this.accountId = accountId;
        this.balances = balances;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Map<String, BigDecimal> getBalances() {
        return balances;
    }

    public void setBalances(Map<String, BigDecimal> balances) {
        this.balances = balances;
    }
}
