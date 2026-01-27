package com.example.banking.controller;

import com.example.banking.dto.CreateTransactionRequest;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionStatus;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void createTransaction_withValidData_returnsCreated() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.50"));
        request.setCurrency("USD");
        request.setType(TransactionType.TRANSFER);

        Transaction transaction = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("100.50"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionService.createTransaction(any(CreateTransactionRequest.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("txn-1"))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.type").value("transfer"))
                .andExpect(jsonPath("$.status").value("completed"));
    }

    @Test
    void createTransaction_withNegativeAmount_returnsBadRequest() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("-100.50"));
        request.setCurrency("USD");
        request.setType(TransactionType.TRANSFER);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void createTransaction_withInvalidCurrency_returnsBadRequest() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.50"));
        request.setCurrency("XYZ");
        request.setType(TransactionType.TRANSFER);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void createTransaction_withInvalidAccountFormat_returnsBadRequest() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("INVALID");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.50"));
        request.setCurrency("USD");
        request.setType(TransactionType.TRANSFER);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void createTransaction_withTooManyDecimalPlaces_returnsBadRequest() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.123"));
        request.setCurrency("USD");
        request.setType(TransactionType.TRANSFER);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void getAllTransactions_returnsListOfTransactions() throws Exception {
        Transaction txn1 = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("100.50"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);

        Transaction txn2 = new Transaction("txn-2", null, "ACC-12345",
                new BigDecimal("50.00"), "EUR", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        List<Transaction> transactions = Arrays.asList(txn1, txn2);

        when(transactionService.getTransactions(null, null, null, null))
                .thenReturn(transactions);

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("txn-1"))
                .andExpect(jsonPath("$[1].id").value("txn-2"));
    }

    @Test
    void getAllTransactions_withAccountIdFilter_returnsFilteredTransactions() throws Exception {
        Transaction txn1 = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("100.50"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionService.getTransactions("ACC-12345", null, null, null))
                .thenReturn(Arrays.asList(txn1));

        mockMvc.perform(get("/transactions")
                        .param("accountId", "ACC-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fromAccount").value("ACC-12345"));
    }

    @Test
    void getAllTransactions_withTypeFilter_returnsFilteredTransactions() throws Exception {
        Transaction txn1 = new Transaction("txn-1", null, "ACC-12345",
                new BigDecimal("100.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionService.getTransactions(null, "deposit", null, null))
                .thenReturn(Arrays.asList(txn1));

        mockMvc.perform(get("/transactions")
                        .param("type", "deposit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("deposit"));
    }

    @Test
    void getAllTransactions_withDateRange_returnsFilteredTransactions() throws Exception {
        Transaction txn1 = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("100.50"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionService.getTransactions(null, null, "2024-01-01", "2024-01-31"))
                .thenReturn(Arrays.asList(txn1));

        mockMvc.perform(get("/transactions")
                        .param("from", "2024-01-01")
                        .param("to", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTransactionById_withValidId_returnsTransaction() throws Exception {
        Transaction transaction = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("100.50"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionService.getTransactionById("txn-1")).thenReturn(transaction);

        mockMvc.perform(get("/transactions/txn-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("txn-1"))
                .andExpect(jsonPath("$.amount").value(100.50));
    }

    @Test
    void getTransactionById_withInvalidId_returnsBadRequest() throws Exception {
        when(transactionService.getTransactionById(anyString()))
                .thenThrow(new IllegalArgumentException("Transaction not found with id: invalid"));

        mockMvc.perform(get("/transactions/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }
}
