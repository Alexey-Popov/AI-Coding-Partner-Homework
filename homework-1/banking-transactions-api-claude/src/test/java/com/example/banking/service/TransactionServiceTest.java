package com.example.banking.service;

import com.example.banking.dto.CreateTransactionRequest;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionStatus;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("100.50"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);
    }

    @Test
    void createTransaction_savesTransactionWithCorrectData() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.50"));
        request.setCurrency("usd");
        request.setType(TransactionType.TRANSFER);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        // Actually call the service method
        transactionService.createTransaction(request);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction capturedTransaction = transactionCaptor.getValue();
        assertThat(capturedTransaction.getId()).isNotNull();
        assertThat(capturedTransaction.getFromAccount()).isEqualTo("ACC-12345");
        assertThat(capturedTransaction.getToAccount()).isEqualTo("ACC-67890");
        assertThat(capturedTransaction.getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(capturedTransaction.getCurrency()).isEqualTo("USD"); // Should be uppercased
        assertThat(capturedTransaction.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(capturedTransaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(capturedTransaction.getTimestamp()).isNotNull();
    }

    @Test
    void createTransaction_convertsCurrencyToUppercase() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.50"));
        request.setCurrency("eur");
        request.setType(TransactionType.TRANSFER);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        transactionService.createTransaction(request);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        assertThat(transactionCaptor.getValue().getCurrency()).isEqualTo("EUR");
    }

    @Test
    void getAllTransactions_returnsAllTransactions() {
        List<Transaction> transactions = Arrays.asList(sampleTransaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("txn-1");
        verify(transactionRepository).findAll();
    }

    @Test
    void getTransactions_withNoFilters_returnsAllTransactions() {
        List<Transaction> transactions = Arrays.asList(sampleTransaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactions(null, null, null, null);

        assertThat(result).hasSize(1);
        verify(transactionRepository).findAll();
    }

    @Test
    void getTransactions_withAccountIdFilter_filtersCorrectly() {
        Transaction txn1 = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("100.50"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);
        Transaction txn2 = new Transaction("txn-2", "ACC-11111", "ACC-22222",
                new BigDecimal("50.00"), "EUR", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionRepository.findAll()).thenReturn(Arrays.asList(txn1, txn2));

        List<Transaction> result = transactionService.getTransactions("ACC-12345", null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFromAccount()).isEqualTo("ACC-12345");
    }

    @Test
    void getTransactions_withTypeFilter_filtersCorrectly() {
        Transaction txn1 = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("100.50"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);
        Transaction txn2 = new Transaction("txn-2", null, "ACC-12345",
                new BigDecimal("50.00"), "EUR", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionRepository.findAll()).thenReturn(Arrays.asList(txn1, txn2));

        List<Transaction> result = transactionService.getTransactions(null, "deposit", null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void getTransactions_withInvalidType_throwsException() {
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(sampleTransaction));

        assertThatThrownBy(() -> transactionService.getTransactions(null, "invalid", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid transaction type");
    }

    @Test
    void getTransactions_withDateRangeFilter_filtersCorrectly() {

        Transaction txn1 = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("100.50"), "USD", TransactionType.TRANSFER,
                Instant.parse("2024-06-15T10:00:00Z"), TransactionStatus.COMPLETED);

        Transaction txn2 = new Transaction("txn-2", "ACC-12345", "ACC-67890",
                new BigDecimal("50.00"), "EUR", TransactionType.DEPOSIT,
                Instant.parse("2023-12-01T10:00:00Z"), TransactionStatus.COMPLETED);

        when(transactionRepository.findAll()).thenReturn(Arrays.asList(txn1, txn2));

        List<Transaction> result = transactionService.getTransactions(
                null, null, "2024-01-01", "2024-12-31");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("txn-1");
    }

    @Test
    void getTransactions_withInvalidFromDate_throwsException() {
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(sampleTransaction));

        assertThatThrownBy(() -> transactionService.getTransactions(null, null, "invalid-date", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid 'from' date format");
    }

    @Test
    void getTransactions_withInvalidToDate_throwsException() {
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(sampleTransaction));

        assertThatThrownBy(() -> transactionService.getTransactions(null, null, null, "invalid-date"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid 'to' date format");
    }

    @Test
    void getTransactions_withMultipleFilters_appliesAllFilters() {
        Transaction txn1 = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("100.50"), "USD", TransactionType.TRANSFER,
                Instant.parse("2024-06-15T10:00:00Z"), TransactionStatus.COMPLETED);

        Transaction txn2 = new Transaction("txn-2", "ACC-12345", "ACC-11111",
                new BigDecimal("50.00"), "EUR", TransactionType.DEPOSIT,
                Instant.parse("2024-06-20T10:00:00Z"), TransactionStatus.COMPLETED);

        Transaction txn3 = new Transaction("txn-3", "ACC-12345", "ACC-22222",
                new BigDecimal("75.00"), "USD", TransactionType.TRANSFER,
                Instant.parse("2024-07-01T10:00:00Z"), TransactionStatus.COMPLETED);

        when(transactionRepository.findAll()).thenReturn(Arrays.asList(txn1, txn2, txn3));

        List<Transaction> result = transactionService.getTransactions(
                "ACC-12345", "transfer", "2024-06-01", "2024-06-30");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("txn-1");
    }

    @Test
    void getTransactionById_withValidId_returnsTransaction() {
        when(transactionRepository.findById("txn-1"))
                .thenReturn(Optional.of(sampleTransaction));

        Transaction result = transactionService.getTransactionById("txn-1");

        assertThat(result.getId()).isEqualTo("txn-1");
        verify(transactionRepository).findById("txn-1");
    }

    @Test
    void getTransactionById_withInvalidId_throwsException() {
        when(transactionRepository.findById("invalid"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction not found with id: invalid");
    }
}
