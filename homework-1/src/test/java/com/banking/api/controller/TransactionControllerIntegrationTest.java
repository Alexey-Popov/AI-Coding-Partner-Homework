package com.banking.api.controller;

import com.banking.api.dto.TransactionRequest;
import com.banking.api.model.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createTransaction_WithValidData_ShouldReturn201() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .fromAccount("ACC-12345")
                .toAccount("ACC-67890")
                .amount(new BigDecimal("100.50"))
                .currency("USD")
                .type(TransactionType.TRANSFER)
                .build();
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fromAccount").value("ACC-12345"))
                .andExpect(jsonPath("$.toAccount").value("ACC-67890"))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    void createTransaction_WithInvalidAccountFormat_ShouldReturn400() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .fromAccount("INVALID")
                .toAccount("ACC-67890")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .type(TransactionType.TRANSFER)
                .build();
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray());
    }
    
    @Test
    void createTransaction_WithNegativeAmount_ShouldReturn400() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .fromAccount("ACC-12345")
                .toAccount("ACC-67890")
                .amount(new BigDecimal("-100.00"))
                .currency("USD")
                .type(TransactionType.TRANSFER)
                .build();
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void createTransaction_WithInvalidCurrency_ShouldReturn400() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .fromAccount("ACC-12345")
                .toAccount("ACC-67890")
                .amount(new BigDecimal("100.00"))
                .currency("XYZ")
                .type(TransactionType.TRANSFER)
                .build();
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getAllTransactions_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void getTransactionById_WhenExists_ShouldReturn200() throws Exception {
        // First create a transaction
        TransactionRequest request = TransactionRequest.builder()
                .fromAccount("ACC-11111")
                .toAccount("ACC-22222")
                .amount(new BigDecimal("50.00"))
                .currency("EUR")
                .type(TransactionType.DEPOSIT)
                .build();
        
        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String responseBody = createResult.getResponse().getContentAsString();
        String transactionId = objectMapper.readTree(responseBody).get("id").asText();
        
        // Then retrieve it
        mockMvc.perform(get("/api/transactions/" + transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.fromAccount").value("ACC-11111"));
    }
    
    @Test
    void getTransactionById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/transactions/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    void getTransactions_WithAccountFilter_ShouldReturnFiltered() throws Exception {
        // Create transactions
        TransactionRequest request1 = TransactionRequest.builder()
                .fromAccount("ACC-AAAAA")
                .toAccount("ACC-BBBBB")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .type(TransactionType.TRANSFER)
                .build();
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        
        // Filter by account
        mockMvc.perform(get("/api/transactions")
                .param("accountId", "ACC-AAAAA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void getTransactions_WithTypeFilter_ShouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .param("type", "DEPOSIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
