package com.example.banking.controller;

import com.example.banking.dto.AccountBalanceResponse;
import com.example.banking.dto.AccountSummaryResponse;
import com.example.banking.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    void getAccountBalance_returnsBalanceForAccount() throws Exception {
        Map<String, BigDecimal> balances = new HashMap<>();
        balances.put("USD", new BigDecimal("1500.50"));
        balances.put("EUR", new BigDecimal("250.00"));

        AccountBalanceResponse response = new AccountBalanceResponse("ACC-12345", balances);

        when(accountService.getAccountBalance("ACC-12345")).thenReturn(response);

        mockMvc.perform(get("/accounts/ACC-12345/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC-12345"))
                .andExpect(jsonPath("$.balances.USD").value(1500.50))
                .andExpect(jsonPath("$.balances.EUR").value(250.00));
    }

    @Test
    void getAccountBalance_withNoTransactions_returnsEmptyBalances() throws Exception {
        Map<String, BigDecimal> balances = new HashMap<>();
        AccountBalanceResponse response = new AccountBalanceResponse("ACC-99999", balances);

        when(accountService.getAccountBalance("ACC-99999")).thenReturn(response);

        mockMvc.perform(get("/accounts/ACC-99999/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC-99999"))
                .andExpect(jsonPath("$.balances").isEmpty());
    }

    @Test
    void getAccountSummary_returnsSummaryForAccount() throws Exception {
        Map<String, BigDecimal> totalDeposits = new HashMap<>();
        totalDeposits.put("USD", new BigDecimal("5000.00"));
        totalDeposits.put("EUR", new BigDecimal("1200.00"));

        Map<String, BigDecimal> totalWithdrawals = new HashMap<>();
        totalWithdrawals.put("USD", new BigDecimal("2500.50"));
        totalWithdrawals.put("EUR", new BigDecimal("300.00"));

        Instant recentDate = Instant.parse("2024-01-26T10:30:00.123Z");

        AccountSummaryResponse response = new AccountSummaryResponse(
                "ACC-12345", totalDeposits, totalWithdrawals, 15, recentDate);

        when(accountService.getAccountSummary("ACC-12345")).thenReturn(response);

        mockMvc.perform(get("/accounts/ACC-12345/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC-12345"))
                .andExpect(jsonPath("$.totalDeposits.USD").value(5000.00))
                .andExpect(jsonPath("$.totalDeposits.EUR").value(1200.00))
                .andExpect(jsonPath("$.totalWithdrawals.USD").value(2500.50))
                .andExpect(jsonPath("$.totalWithdrawals.EUR").value(300.00))
                .andExpect(jsonPath("$.numberOfTransactions").value(15))
                .andExpect(jsonPath("$.mostRecentTransactionDate").value("2024-01-26T10:30:00.123Z"));
    }

    @Test
    void getAccountSummary_withNoTransactions_returnsZeroSummary() throws Exception {
        Map<String, BigDecimal> emptyMap = new HashMap<>();

        AccountSummaryResponse response = new AccountSummaryResponse(
                "ACC-99999", emptyMap, emptyMap, 0, null);

        when(accountService.getAccountSummary("ACC-99999")).thenReturn(response);

        mockMvc.perform(get("/accounts/ACC-99999/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC-99999"))
                .andExpect(jsonPath("$.numberOfTransactions").value(0))
                .andExpect(jsonPath("$.mostRecentTransactionDate").doesNotExist());
    }
}
