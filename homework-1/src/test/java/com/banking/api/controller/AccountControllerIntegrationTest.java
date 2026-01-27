package com.banking.api.controller;

import com.banking.api.dto.TransactionRequest;
import com.banking.api.model.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create some test transactions
        TransactionRequest depositRequest = TransactionRequest.builder()
                .fromAccount(null)
                .toAccount("ACC-TEST1")
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .type(TransactionType.DEPOSIT)
                .build();
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)));
        
        TransactionRequest withdrawalRequest = TransactionRequest.builder()
                .fromAccount("ACC-TEST1")
                .toAccount(null)
                .amount(new BigDecimal("250.00"))
                .currency("USD")
                .type(TransactionType.WITHDRAWAL)
                .build();
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawalRequest)));
    }
    
    @Test
    void getBalance_ShouldReturnAccountBalance() throws Exception {
        mockMvc.perform(get("/api/accounts/ACC-TEST1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC-TEST1"))
                .andExpect(jsonPath("$.balance").exists())
                .andExpect(jsonPath("$.currency").value("USD"));
    }
    
    @Test
    void getAccountSummary_ShouldReturnSummary() throws Exception {
        mockMvc.perform(get("/api/accounts/ACC-TEST1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC-TEST1"))
                .andExpect(jsonPath("$.totalDeposits").exists())
                .andExpect(jsonPath("$.totalWithdrawals").exists())
                .andExpect(jsonPath("$.numberOfTransactions").exists())
                .andExpect(jsonPath("$.mostRecentTransactionDate").exists());
    }
    
    @Test
    void calculateInterest_ShouldReturnInterestCalculation() throws Exception {
        mockMvc.perform(get("/api/accounts/ACC-TEST1/interest")
                .param("rate", "0.05")
                .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC-TEST1"))
                .andExpect(jsonPath("$.principal").exists())
                .andExpect(jsonPath("$.rate").value(0.05))
                .andExpect(jsonPath("$.days").value(30))
                .andExpect(jsonPath("$.interest").exists())
                .andExpect(jsonPath("$.totalAmount").exists());
    }
}
