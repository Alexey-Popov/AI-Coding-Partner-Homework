package com.banking.api.service;

import com.banking.api.dto.TransactionRequest;
import com.banking.api.dto.TransactionResponse;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.model.Transaction;
import com.banking.api.model.TransactionStatus;
import com.banking.api.model.TransactionType;
import com.banking.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @InjectMocks
    private TransactionService transactionService;
    
    private Transaction sampleTransaction;
    private TransactionRequest sampleRequest;
    
    @BeforeEach
    void setUp() {
        sampleTransaction = Transaction.builder()
                .id("test-id-123")
                .fromAccount("ACC-12345")
                .toAccount("ACC-67890")
                .amount(new BigDecimal("100.50"))
                .currency("USD")
                .type(TransactionType.TRANSFER)
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
        
        sampleRequest = TransactionRequest.builder()
                .fromAccount("ACC-12345")
                .toAccount("ACC-67890")
                .amount(new BigDecimal("100.50"))
                .currency("USD")
                .type(TransactionType.TRANSFER)
                .build();
    }
    
    @Test
    void createTransaction_ShouldReturnTransactionResponse() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);
        
        TransactionResponse response = transactionService.createTransaction(sampleRequest);
        
        assertNotNull(response);
        assertEquals("ACC-12345", response.getFromAccount());
        assertEquals("ACC-67890", response.getToAccount());
        assertEquals(new BigDecimal("100.50"), response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals(TransactionType.TRANSFER, response.getType());
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
        
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
    
    @Test
    void getTransactionById_WhenExists_ShouldReturnTransaction() {
        when(transactionRepository.findById("test-id-123")).thenReturn(Optional.of(sampleTransaction));
        
        TransactionResponse response = transactionService.getTransactionById("test-id-123");
        
        assertNotNull(response);
        assertEquals("test-id-123", response.getId());
        assertEquals("ACC-12345", response.getFromAccount());
        
        verify(transactionRepository, times(1)).findById("test-id-123");
    }
    
    @Test
    void getTransactionById_WhenNotExists_ShouldThrowException() {
        when(transactionRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.getTransactionById("non-existent");
        });
        
        verify(transactionRepository, times(1)).findById("non-existent");
    }
    
    @Test
    void getAllTransactions_ShouldReturnAllTransactions() {
        Transaction transaction2 = Transaction.builder()
                .id("test-id-456")
                .fromAccount("ACC-11111")
                .toAccount("ACC-22222")
                .amount(new BigDecimal("50.00"))
                .currency("EUR")
                .type(TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
        
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(sampleTransaction, transaction2));
        
        List<TransactionResponse> responses = transactionService.getAllTransactions();
        
        assertEquals(2, responses.size());
        verify(transactionRepository, times(1)).findAll();
    }
    
    @Test
    void getFilteredTransactions_ShouldCallRepositoryWithFilters() {
        when(transactionRepository.findWithFilters(any(), any(), any(), any()))
                .thenReturn(Arrays.asList(sampleTransaction));
        
        List<TransactionResponse> responses = transactionService.getFilteredTransactions(
                "ACC-12345", 
                TransactionType.TRANSFER, 
                LocalDateTime.now().minusDays(1), 
                LocalDateTime.now()
        );
        
        assertEquals(1, responses.size());
        verify(transactionRepository, times(1)).findWithFilters(any(), any(), any(), any());
    }
}
