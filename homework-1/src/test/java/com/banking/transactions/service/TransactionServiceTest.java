package com.banking.transactions.service;

import com.banking.transactions.dto.AccountSummary;
import com.banking.transactions.exception.ValidationException;
import com.banking.transactions.model.Transaction;
import com.banking.transactions.util.ValidationMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionService.
 * Tests transaction creation, validation, filtering, and statistics.
 */
class TransactionServiceTest {

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService();
    }

    @Test
    void testCreateValidTransaction() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setFromAccount("ACC-12345");
        transaction.setToAccount("ACC-67890");
        transaction.setAmount(new BigDecimal("100.50"));
        transaction.setCurrency("USD");
        transaction.setType(Transaction.TransactionType.TRANSFER);

        // Act
        Transaction created = transactionService.createTransaction(transaction);

        // Assert
        assertNotNull(created.getId());
        assertEquals("ACC-12345", created.getFromAccount());
        assertEquals("ACC-67890", created.getToAccount());
        assertEquals(new BigDecimal("100.50"), created.getAmount());
        assertEquals("USD", created.getCurrency());
        assertEquals(Transaction.TransactionType.TRANSFER, created.getType());
        assertNotNull(created.getTimestamp());
        assertEquals(Transaction.TransactionStatus.COMPLETED, created.getStatus());
    }

    @Test
    void testCreateTransaction_MissingFromAccount() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setToAccount("ACC-67890");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency("USD");
        transaction.setType(Transaction.TransactionType.TRANSFER);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(transaction);
        });

        assertTrue(exception.getErrorResponse().getDetails().stream()
            .anyMatch(d -> d.getField().equals("fromAccount") && 
                          d.getMessage().equals(ValidationMessages.ACCOUNT_REQUIRED)));
    }

    @Test
    void testCreateTransaction_InvalidAccountFormat() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setFromAccount("INVALID");
        transaction.setToAccount("ACC-67890");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency("USD");
        transaction.setType(Transaction.TransactionType.TRANSFER);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(transaction);
        });

        assertTrue(exception.getErrorResponse().getDetails().stream()
            .anyMatch(d -> d.getField().equals("fromAccount") && 
                          d.getMessage().equals(ValidationMessages.ACCOUNT_FORMAT)));
    }

    @Test
    void testCreateTransaction_NegativeAmount() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setFromAccount("ACC-12345");
        transaction.setToAccount("ACC-67890");
        transaction.setAmount(new BigDecimal("-50.00"));
        transaction.setCurrency("USD");
        transaction.setType(Transaction.TransactionType.TRANSFER);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(transaction);
        });

        assertTrue(exception.getErrorResponse().getDetails().stream()
            .anyMatch(d -> d.getField().equals("amount") && 
                          d.getMessage().equals(ValidationMessages.AMOUNT_POSITIVE)));
    }

    @Test
    void testCreateTransaction_TooManyDecimalPlaces() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setFromAccount("ACC-12345");
        transaction.setToAccount("ACC-67890");
        transaction.setAmount(new BigDecimal("100.123"));
        transaction.setCurrency("USD");
        transaction.setType(Transaction.TransactionType.TRANSFER);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(transaction);
        });

        assertTrue(exception.getErrorResponse().getDetails().stream()
            .anyMatch(d -> d.getField().equals("amount") && 
                          d.getMessage().equals(ValidationMessages.AMOUNT_DECIMAL_PLACES)));
    }

    @Test
    void testCreateTransaction_InvalidCurrency() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setFromAccount("ACC-12345");
        transaction.setToAccount("ACC-67890");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency("XXX");
        transaction.setType(Transaction.TransactionType.TRANSFER);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(transaction);
        });

        assertTrue(exception.getErrorResponse().getDetails().stream()
            .anyMatch(d -> d.getField().equals("currency") && 
                          d.getMessage().equals(ValidationMessages.CURRENCY_INVALID)));
    }

    @Test
    void testCreateTransaction_SameFromAndToAccount() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setFromAccount("ACC-12345");
        transaction.setToAccount("ACC-12345");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency("USD");
        transaction.setType(Transaction.TransactionType.TRANSFER);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(transaction);
        });

        assertTrue(exception.getErrorResponse().getDetails().stream()
            .anyMatch(d -> d.getField().equals("accounts") && 
                          d.getMessage().equals(ValidationMessages.TRANSACTION_SAME_ACCOUNT)));
    }

    @Test
    void testGetAllTransactions() {
        // Arrange
        Transaction transaction1 = createValidTransaction("ACC-11111", "ACC-22222", "50.00", "USD");
        Transaction transaction2 = createValidTransaction("ACC-33333", "ACC-44444", "75.00", "EUR");

        transactionService.createTransaction(transaction1);
        transactionService.createTransaction(transaction2);

        // Act
        List<Transaction> transactions = transactionService.getAllTransactions();

        // Assert
        assertEquals(2, transactions.size());
    }

    @Test
    void testGetTransactionById() {
        // Arrange
        Transaction transaction = createValidTransaction("ACC-11111", "ACC-22222", "50.00", "USD");
        Transaction created = transactionService.createTransaction(transaction);

        // Act
        Transaction found = transactionService.getTransactionById(created.getId());

        // Assert
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("ACC-11111", found.getFromAccount());
    }

    @Test
    void testGetTransactionById_NotFound() {
        // Act
        Transaction found = transactionService.getTransactionById("nonexistent-id");

        // Assert
        assertNull(found);
    }

    @Test
    void testFilterTransactionsByAccountId() {
        // Arrange
        Transaction t1 = createValidTransaction("ACC-11111", "ACC-22222", "50.00", "USD");
        Transaction t2 = createValidTransaction("ACC-33333", "ACC-11111", "75.00", "EUR");
        Transaction t3 = createValidTransaction("ACC-55555", "ACC-66666", "100.00", "GBP");

        transactionService.createTransaction(t1);
        transactionService.createTransaction(t2);
        transactionService.createTransaction(t3);

        // Act
        List<Transaction> filtered = transactionService.getFilteredTransactions("ACC-11111", null, null, null);

        // Assert
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(t -> 
            t.getFromAccount().equals("ACC-11111") || t.getToAccount().equals("ACC-11111")));
    }

    @Test
    void testFilterTransactionsByType() {
        // Arrange
        Transaction t1 = createTransactionWithType("ACC-11111", "ACC-22222", "50.00", "USD", Transaction.TransactionType.DEPOSIT);
        Transaction t2 = createTransactionWithType("ACC-33333", "ACC-44444", "75.00", "EUR", Transaction.TransactionType.WITHDRAWAL);
        Transaction t3 = createTransactionWithType("ACC-55555", "ACC-66666", "100.00", "GBP", Transaction.TransactionType.TRANSFER);

        transactionService.createTransaction(t1);
        transactionService.createTransaction(t2);
        transactionService.createTransaction(t3);

        // Act
        List<Transaction> filtered = transactionService.getFilteredTransactions(null, "deposit", null, null);

        // Assert
        assertEquals(1, filtered.size());
        assertEquals(Transaction.TransactionType.DEPOSIT, filtered.get(0).getType());
    }

    @Test
    void testGetAccountSummary() {
        // Arrange
        Transaction deposit = createTransactionWithType("ACC-99999", "ACC-11111", "100.00", "USD", Transaction.TransactionType.DEPOSIT);
        Transaction withdrawal = createTransactionWithType("ACC-11111", "ACC-88888", "50.00", "USD", Transaction.TransactionType.WITHDRAWAL);
        Transaction transfer = createTransactionWithType("ACC-11111", "ACC-77777", "25.00", "USD", Transaction.TransactionType.TRANSFER);

        transactionService.createTransaction(deposit);
        transactionService.createTransaction(withdrawal);
        transactionService.createTransaction(transfer);

        // Act
        AccountSummary summary = transactionService.getAccountSummary("ACC-11111");

        // Assert
        assertEquals(1, summary.getTotalDeposits());
        assertEquals(1, summary.getTotalWithdrawals());
        assertEquals(3, summary.getNumberOfTransactions());
        assertNotNull(summary.getMostRecentTransactionDate());
    }

    @Test
    void testGetAccountSummary_NoTransactions() {
        // Act
        AccountSummary summary = transactionService.getAccountSummary("ACC-99999");

        // Assert
        assertEquals(0, summary.getTotalDeposits());
        assertEquals(0, summary.getTotalWithdrawals());
        assertEquals(0, summary.getNumberOfTransactions());
        assertNull(summary.getMostRecentTransactionDate());
    }

    // Helper methods
    private Transaction createValidTransaction(String fromAccount, String toAccount, String amount, String currency) {
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setCurrency(currency);
        transaction.setType(Transaction.TransactionType.TRANSFER);
        return transaction;
    }

    private Transaction createTransactionWithType(String fromAccount, String toAccount, String amount, String currency, Transaction.TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setCurrency(currency);
        transaction.setType(type);
        return transaction;
    }
}
