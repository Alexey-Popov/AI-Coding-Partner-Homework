package com.banking.transactions.controller;

import com.banking.transactions.model.Transaction;
import com.banking.transactions.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TransactionController.
 * Tests REST endpoints with mocked service layer.
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void testCreateTransaction_Success() throws Exception {
        // Arrange
        Transaction input = createTransaction("ACC-12345", "ACC-67890", "100.50", "USD", Transaction.TransactionType.TRANSFER);
        
        Transaction output = createTransaction("ACC-12345", "ACC-67890", "100.50", "USD", Transaction.TransactionType.TRANSFER);
        output.setId("test-id-123");
        output.setTimestamp(LocalDateTime.now());
        output.setStatus(Transaction.TransactionStatus.COMPLETED);

        when(transactionService.createTransaction(any(Transaction.class))).thenReturn(output);

        // Act & Assert
        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("test-id-123"))
                .andExpect(jsonPath("$.fromAccount").value("ACC-12345"))
                .andExpect(jsonPath("$.toAccount").value("ACC-67890"))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.type").value("transfer"))
                .andExpect(jsonPath("$.status").value("completed"));
    }

    @Test
    void testGetAllTransactions() throws Exception {
        // Arrange
        Transaction t1 = createTransactionWithId("id-1", "ACC-11111", "ACC-22222", "50.00", "USD");
        Transaction t2 = createTransactionWithId("id-2", "ACC-33333", "ACC-44444", "75.00", "EUR");
        
        when(transactionService.getAllTransactions()).thenReturn(Arrays.asList(t1, t2));

        // Act & Assert
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[1].id").value("id-2"));
    }

    @Test
    void testGetAllTransactions_WithFilters() throws Exception {
        // Arrange
        Transaction t1 = createTransactionWithId("id-1", "ACC-12345", "ACC-22222", "50.00", "USD");
        
        when(transactionService.getFilteredTransactions("ACC-12345", "transfer", null, null))
                .thenReturn(Arrays.asList(t1));

        // Act & Assert
        mockMvc.perform(get("/transactions")
                .param("accountId", "ACC-12345")
                .param("type", "transfer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("id-1"));
    }

    @Test
    void testGetTransactionById_Found() throws Exception {
        // Arrange
        Transaction transaction = createTransactionWithId("id-123", "ACC-11111", "ACC-22222", "100.00", "USD");
        
        when(transactionService.getTransactionById("id-123")).thenReturn(transaction);

        // Act & Assert
        mockMvc.perform(get("/transactions/id-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-123"))
                .andExpect(jsonPath("$.fromAccount").value("ACC-11111"));
    }

    @Test
    void testGetTransactionById_NotFound() throws Exception {
        // Arrange
        when(transactionService.getTransactionById("nonexistent")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/transactions/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllTransactions_Empty() throws Exception {
        // Arrange
        when(transactionService.getAllTransactions()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // Helper methods
    private Transaction createTransaction(String fromAccount, String toAccount, String amount, String currency, Transaction.TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setCurrency(currency);
        transaction.setType(type);
        return transaction;
    }

    private Transaction createTransactionWithId(String id, String fromAccount, String toAccount, String amount, String currency) {
        Transaction transaction = createTransaction(fromAccount, toAccount, amount, currency, Transaction.TransactionType.TRANSFER);
        transaction.setId(id);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        return transaction;
    }
}
