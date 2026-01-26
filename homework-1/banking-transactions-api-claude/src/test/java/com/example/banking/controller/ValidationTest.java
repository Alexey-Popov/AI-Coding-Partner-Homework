package com.example.banking.controller;

import com.example.banking.dto.CreateTransactionRequest;
import com.example.banking.model.TransactionType;
import com.example.banking.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsInAnyOrder;

@WebMvcTest(TransactionController.class)
class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void createTransaction_withBothAccountsMissing_returnsBothErrors() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setType(TransactionType.DEPOSIT);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[*].field", containsInAnyOrder("fromAccount", "toAccount")));
    }

    @Test
    void createTransaction_withOnlyFromAccountMissing_returnsFromAccountError() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setToAccount("ACC-12345");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setType(TransactionType.DEPOSIT);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[?(@.field == 'fromAccount')].message").value("From account is required"));
    }

    @Test
    void createTransaction_withOnlyToAccountMissing_returnsToAccountError() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setType(TransactionType.DEPOSIT);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details[?(@.field == 'toAccount')].message").value("To account is required"));
    }
}

