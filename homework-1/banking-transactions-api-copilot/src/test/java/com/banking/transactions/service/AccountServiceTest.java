package com.banking.transactions.service;

import com.banking.transactions.dto.BalanceResponse;
import com.banking.transactions.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AccountService.
 * Tests account balance calculations and multi-currency support.
 */
class AccountServiceTest {

    @Mock
    private TransactionService transactionService;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(transactionService);
    }

    @Test
    void testGetAccountBalance_SingleCurrency() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(createCompletedTransaction("ACC-99999", "ACC-12345", "100.00", "USD", Transaction.TransactionType.DEPOSIT));
        transactions.add(createCompletedTransaction("ACC-12345", "ACC-88888", "30.00", "USD", Transaction.TransactionType.WITHDRAWAL));

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        // Act
        BalanceResponse balance = accountService.getAccountBalance("ACC-12345");

        // Assert
        assertEquals(1, balance.getBalances().size());
        assertEquals("USD", balance.getBalances().get(0).getCurrency());
        assertEquals(0, new BigDecimal("70.00").compareTo(balance.getBalances().get(0).getBalance()));
    }

    @Test
    void testGetAccountBalance_MultiCurrency() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(createCompletedTransaction("ACC-99999", "ACC-12345", "100.00", "USD", Transaction.TransactionType.DEPOSIT));
        transactions.add(createCompletedTransaction("ACC-88888", "ACC-12345", "50.00", "EUR", Transaction.TransactionType.DEPOSIT));
        transactions.add(createCompletedTransaction("ACC-12345", "ACC-77777", "20.00", "USD", Transaction.TransactionType.WITHDRAWAL));

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        // Act
        BalanceResponse balance = accountService.getAccountBalance("ACC-12345");

        // Assert
        assertEquals(2, balance.getBalances().size());
        // Find USD and EUR balances
        BigDecimal usdBalance = balance.getBalances().stream()
            .filter(b -> b.getCurrency().equals("USD"))
            .map(b -> b.getBalance())
            .findFirst().orElse(null);
        BigDecimal eurBalance = balance.getBalances().stream()
            .filter(b -> b.getCurrency().equals("EUR"))
            .map(b -> b.getBalance())
            .findFirst().orElse(null);
        
        assertNotNull(usdBalance);
        assertNotNull(eurBalance);
        assertEquals(0, new BigDecimal("80.00").compareTo(usdBalance));
        assertEquals(0, new BigDecimal("50.00").compareTo(eurBalance));
    }

    @Test
    void testGetAccountBalance_TransferOperations() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(createCompletedTransaction("ACC-99999", "ACC-12345", "200.00", "USD", Transaction.TransactionType.DEPOSIT));
        transactions.add(createCompletedTransaction("ACC-12345", "ACC-88888", "50.00", "USD", Transaction.TransactionType.TRANSFER));
        transactions.add(createCompletedTransaction("ACC-77777", "ACC-12345", "75.00", "USD", Transaction.TransactionType.TRANSFER));

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        // Act
        BalanceResponse balance = accountService.getAccountBalance("ACC-12345");

        // Assert
        assertEquals(1, balance.getBalances().size());
        // 200 (deposit) - 50 (outgoing transfer) + 75 (incoming transfer) = 225
        assertEquals("USD", balance.getBalances().get(0).getCurrency());
        assertEquals(0, new BigDecimal("225.00").compareTo(balance.getBalances().get(0).getBalance()));
    }

    @Test
    void testGetAccountBalance_NoTransactions() {
        // Arrange
        when(transactionService.getAllTransactions()).thenReturn(new ArrayList<>());

        // Act
        BalanceResponse balance = accountService.getAccountBalance("ACC-12345");

        // Assert
        assertEquals(0, balance.getBalances().size());
    }

    @Test
    void testGetAccountBalance_IgnoresPendingTransactions() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        Transaction pending = createTransaction("ACC-99999", "ACC-12345", "100.00", "USD", Transaction.TransactionType.DEPOSIT);
        pending.setStatus(Transaction.TransactionStatus.PENDING);
        transactions.add(pending);

        Transaction completed = createCompletedTransaction("ACC-88888", "ACC-12345", "50.00", "USD", Transaction.TransactionType.DEPOSIT);
        transactions.add(completed);

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        // Act
        BalanceResponse balance = accountService.getAccountBalance("ACC-12345");

        // Assert
        assertEquals(1, balance.getBalances().size());
        assertEquals("USD", balance.getBalances().get(0).getCurrency());
        assertEquals(0, new BigDecimal("50.00").compareTo(balance.getBalances().get(0).getBalance()));
    }

    @Test
    void testGetAccountBalance_IgnoresFailedTransactions() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        Transaction failed = createTransaction("ACC-99999", "ACC-12345", "100.00", "USD", Transaction.TransactionType.DEPOSIT);
        failed.setStatus(Transaction.TransactionStatus.FAILED);
        transactions.add(failed);

        Transaction completed = createCompletedTransaction("ACC-88888", "ACC-12345", "50.00", "USD", Transaction.TransactionType.DEPOSIT);
        transactions.add(completed);

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        // Act
        BalanceResponse balance = accountService.getAccountBalance("ACC-12345");

        // Assert
        assertEquals(1, balance.getBalances().size());
        assertEquals("USD", balance.getBalances().get(0).getCurrency());
        assertEquals(0, new BigDecimal("50.00").compareTo(balance.getBalances().get(0).getBalance()));
    }

    // Helper methods
    private Transaction createCompletedTransaction(String fromAccount, String toAccount, String amount, String currency, Transaction.TransactionType type) {
        Transaction transaction = createTransaction(fromAccount, toAccount, amount, currency, type);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        return transaction;
    }

    private Transaction createTransaction(String fromAccount, String toAccount, String amount, String currency, Transaction.TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setId(java.util.UUID.randomUUID().toString());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setCurrency(currency);
        transaction.setType(type);
        transaction.setTimestamp(java.time.LocalDateTime.now());
        return transaction;
    }
}
