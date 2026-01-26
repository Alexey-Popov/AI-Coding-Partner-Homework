package com.banking.transactions.controller;

import com.banking.transactions.dto.AccountSummary;
import com.banking.transactions.dto.BalanceResponse;
import com.banking.transactions.service.AccountService;
import com.banking.transactions.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AccountController.
 * Tests account balance and summary endpoints with mocked service layer.
 */
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    @Test
    void testGetAccountBalance_SingleCurrency() throws Exception {
        // Arrange
        List<BalanceResponse.CurrencyBalance> balances = new ArrayList<>();
        balances.add(new BalanceResponse.CurrencyBalance("USD", new BigDecimal("100.50")));
        
        BalanceResponse balanceResponse = new BalanceResponse("ACC-12345", balances);
        when(accountService.getAccountBalance("ACC-12345")).thenReturn(balanceResponse);

        // Act & Assert
        mockMvc.perform(get("/accounts/ACC-12345/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balances[0].currency").value("USD"))
                .andExpect(jsonPath("$.balances[0].balance").value(100.50));
    }

    @Test
    void testGetAccountBalance_MultiCurrency() throws Exception {
        // Arrange
        List<BalanceResponse.CurrencyBalance> balances = new ArrayList<>();
        balances.add(new BalanceResponse.CurrencyBalance("USD", new BigDecimal("100.00")));
        balances.add(new BalanceResponse.CurrencyBalance("EUR", new BigDecimal("50.00")));
        balances.add(new BalanceResponse.CurrencyBalance("GBP", new BigDecimal("75.00")));
        
        BalanceResponse balanceResponse = new BalanceResponse("ACC-12345", balances);
        when(accountService.getAccountBalance("ACC-12345")).thenReturn(balanceResponse);

        // Act & Assert
        mockMvc.perform(get("/accounts/ACC-12345/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balances[?(@.currency == 'USD')].balance").value(100.00))
                .andExpect(jsonPath("$.balances[?(@.currency == 'EUR')].balance").value(50.00))
                .andExpect(jsonPath("$.balances[?(@.currency == 'GBP')].balance").value(75.00));
    }

    @Test
    void testGetAccountBalance_EmptyAccount() throws Exception {
        // Arrange
        BalanceResponse balanceResponse = new BalanceResponse("ACC-99999", new ArrayList<>());
        when(accountService.getAccountBalance("ACC-99999")).thenReturn(balanceResponse);

        // Act & Assert
        mockMvc.perform(get("/accounts/ACC-99999/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balances").isEmpty());
    }

    @Test
    void testGetAccountSummary() throws Exception {
        // Arrange
        AccountSummary summary = new AccountSummary(5, 3, 10, LocalDateTime.of(2026, 1, 24, 12, 0, 0));
        when(transactionService.getAccountSummary("ACC-12345")).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/accounts/ACC-12345/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeposits").value(5))
                .andExpect(jsonPath("$.totalWithdrawals").value(3))
                .andExpect(jsonPath("$.numberOfTransactions").value(10))
                .andExpect(jsonPath("$.mostRecentTransactionDate").value("2026-01-24T12:00:00"));
    }

    @Test
    void testGetAccountSummary_NoTransactions() throws Exception {
        // Arrange
        AccountSummary summary = new AccountSummary(0, 0, 0, null);
        when(transactionService.getAccountSummary("ACC-99999")).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/accounts/ACC-99999/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeposits").value(0))
                .andExpect(jsonPath("$.totalWithdrawals").value(0))
                .andExpect(jsonPath("$.numberOfTransactions").value(0))
                .andExpect(jsonPath("$.mostRecentTransactionDate").doesNotExist());
    }
}
