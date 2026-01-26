package com.banking.api.service;

import com.banking.api.dto.BalanceResponse;
import com.banking.api.dto.InterestResponse;
import com.banking.api.dto.SummaryResponse;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @InjectMocks
    private AccountService accountService;
    
    private Transaction depositTransaction;
    private Transaction withdrawalTransaction;
    
    @BeforeEach
    void setUp() {
        depositTransaction = Transaction.builder()
                .id("deposit-1")
                .fromAccount(null)
                .toAccount("ACC-12345")
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .type(TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now().minusDays(2))
                .status(TransactionStatus.COMPLETED)
                .build();
        
        withdrawalTransaction = Transaction.builder()
                .id("withdrawal-1")
                .fromAccount("ACC-12345")
                .toAccount(null)
                .amount(new BigDecimal("250.00"))
                .currency("USD")
                .type(TransactionType.WITHDRAWAL)
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .build();
    }
    
    @Test
    void getBalance_ShouldReturnCorrectBalance() {
        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(depositTransaction, withdrawalTransaction));
        
        BalanceResponse response = accountService.getBalance("ACC-12345");
        
        assertNotNull(response);
        assertEquals("ACC-12345", response.getAccountId());
        assertEquals(new BigDecimal("750.00"), response.getBalance());
        assertEquals("USD", response.getCurrency());
        
        verify(transactionRepository, times(1)).findByAccountId("ACC-12345");
    }
    
    @Test
    void calculateBalance_WithDepositsAndWithdrawals_ShouldReturnCorrectBalance() {
        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(depositTransaction, withdrawalTransaction));
        
        BigDecimal balance = accountService.calculateBalance("ACC-12345");
        
        assertEquals(new BigDecimal("750.00"), balance);
    }
    
    @Test
    void calculateBalance_WithNoTransactions_ShouldReturnZero() {
        when(transactionRepository.findByAccountId("ACC-99999"))
                .thenReturn(Collections.emptyList());
        
        BigDecimal balance = accountService.calculateBalance("ACC-99999");
        
        assertEquals(BigDecimal.ZERO, balance);
    }
    
    @Test
    void getAccountSummary_ShouldReturnCorrectSummary() {
        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(depositTransaction, withdrawalTransaction));
        
        SummaryResponse response = accountService.getAccountSummary("ACC-12345");
        
        assertNotNull(response);
        assertEquals("ACC-12345", response.getAccountId());
        assertEquals(new BigDecimal("1000.00"), response.getTotalDeposits());
        assertEquals(new BigDecimal("250.00"), response.getTotalWithdrawals());
        assertEquals(2, response.getNumberOfTransactions());
        assertNotNull(response.getMostRecentTransactionDate());
    }
    
    @Test
    void getAccountSummary_WithNoTransactions_ShouldReturnZeroSummary() {
        when(transactionRepository.findByAccountId("ACC-99999"))
                .thenReturn(Collections.emptyList());
        
        SummaryResponse response = accountService.getAccountSummary("ACC-99999");
        
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getTotalDeposits());
        assertEquals(BigDecimal.ZERO, response.getTotalWithdrawals());
        assertEquals(0, response.getNumberOfTransactions());
        assertNull(response.getMostRecentTransactionDate());
    }
    
    @Test
    void calculateInterest_ShouldReturnCorrectInterest() {
        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(depositTransaction));
        
        InterestResponse response = accountService.calculateInterest("ACC-12345", 0.05, 30);
        
        assertNotNull(response);
        assertEquals("ACC-12345", response.getAccountId());
        assertEquals(new BigDecimal("1000.00"), response.getPrincipal());
        assertEquals(0.05, response.getRate());
        assertEquals(30, response.getDays());
        assertTrue(response.getInterest().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getTotalAmount().compareTo(response.getPrincipal()) > 0);
    }
}
