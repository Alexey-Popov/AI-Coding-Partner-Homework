package com.banking.transactions.dto;

import java.math.BigDecimal;
import java.util.List;

public class BalanceResponse {
    
    private String accountId;
    private List<CurrencyBalance> balances;

    public BalanceResponse() {
    }

    public BalanceResponse(String accountId, List<CurrencyBalance> balances) {
        this.accountId = accountId;
        this.balances = balances;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public List<CurrencyBalance> getBalances() {
        return balances;
    }

    public void setBalances(List<CurrencyBalance> balances) {
        this.balances = balances;
    }

    public static class CurrencyBalance {
        private String currency;
        private BigDecimal balance;

        public CurrencyBalance() {
        }

        public CurrencyBalance(String currency, BigDecimal balance) {
            this.currency = currency;
            this.balance = balance;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
    }
}
