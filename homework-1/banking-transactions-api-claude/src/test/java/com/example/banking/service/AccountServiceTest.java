package com.example.banking.service;

import com.example.banking.dto.AccountBalanceResponse;
import com.example.banking.dto.AccountSummaryResponse;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionStatus;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void getAccountBalance_withDeposits_calculatesCorrectBalance() {
        Transaction deposit1 = new Transaction("txn-1", null, "ACC-12345",
                new BigDecimal("500.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        Transaction deposit2 = new Transaction("txn-2", null, "ACC-12345",
                new BigDecimal("300.50"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(deposit1, deposit2));

        AccountBalanceResponse result = accountService.getAccountBalance("ACC-12345");

        assertThat(result.getAccountId()).isEqualTo("ACC-12345");
        assertThat(result.getBalances()).containsEntry("USD", new BigDecimal("800.50"));
    }

    @Test
    void getAccountBalance_withWithdrawals_calculatesCorrectBalance() {
        Transaction deposit = new Transaction("txn-1", null, "ACC-12345",
                new BigDecimal("1000.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        Transaction withdrawal = new Transaction("txn-2", "ACC-12345", null,
                new BigDecimal("300.00"), "USD", TransactionType.WITHDRAWAL,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(deposit, withdrawal));

        AccountBalanceResponse result = accountService.getAccountBalance("ACC-12345");

        assertThat(result.getBalances()).containsEntry("USD", new BigDecimal("700.00"));
    }

    @Test
    void getAccountBalance_withTransfers_calculatesCorrectBalance() {
        Transaction transferIn = new Transaction("txn-1", "ACC-67890", "ACC-12345",
                new BigDecimal("500.00"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);

        Transaction transferOut = new Transaction("txn-2", "ACC-12345", "ACC-11111",
                new BigDecimal("200.00"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(transferIn, transferOut));

        AccountBalanceResponse result = accountService.getAccountBalance("ACC-12345");

        assertThat(result.getBalances()).containsEntry("USD", new BigDecimal("300.00"));
    }

    @Test
    void getAccountBalance_withMultipleCurrencies_calculatesCorrectBalances() {
        Transaction depositUSD = new Transaction("txn-1", null, "ACC-12345",
                new BigDecimal("1000.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        Transaction depositEUR = new Transaction("txn-2", null, "ACC-12345",
                new BigDecimal("500.00"), "EUR", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        Transaction withdrawalUSD = new Transaction("txn-3", "ACC-12345", null,
                new BigDecimal("200.00"), "USD", TransactionType.WITHDRAWAL,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(depositUSD, depositEUR, withdrawalUSD));

        AccountBalanceResponse result = accountService.getAccountBalance("ACC-12345");

        assertThat(result.getBalances()).containsEntry("USD", new BigDecimal("800.00"));
        assertThat(result.getBalances()).containsEntry("EUR", new BigDecimal("500.00"));
    }

    @Test
    void getAccountBalance_onlyCountsCompletedTransactions() {
        Transaction completed = new Transaction("txn-1", null, "ACC-12345",
                new BigDecimal("500.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        Transaction pending = new Transaction("txn-2", null, "ACC-12345",
                new BigDecimal("300.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.PENDING);

        Transaction failed = new Transaction("txn-3", null, "ACC-12345",
                new BigDecimal("200.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.FAILED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(completed, pending, failed));

        AccountBalanceResponse result = accountService.getAccountBalance("ACC-12345");

        assertThat(result.getBalances()).containsEntry("USD", new BigDecimal("500.00"));
    }

    @Test
    void getAccountBalance_withNoTransactions_returnsEmptyBalances() {
        when(transactionRepository.findByAccountId("ACC-99999"))
                .thenReturn(Collections.emptyList());

        AccountBalanceResponse result = accountService.getAccountBalance("ACC-99999");

        assertThat(result.getAccountId()).isEqualTo("ACC-99999");
        assertThat(result.getBalances()).isEmpty();
    }

    @Test
    void getAccountSummary_calculatesCorrectTotals() {
        Instant instant1 = Instant.parse("2024-01-15T10:00:00Z");
        Instant instant2 = Instant.parse("2024-01-20T10:00:00Z");
        Instant instant3 = Instant.parse("2024-01-25T10:00:00Z");

        Transaction deposit = new Transaction("txn-1", null, "ACC-12345",
                new BigDecimal("1000.00"), "USD", TransactionType.DEPOSIT,
                instant1, TransactionStatus.COMPLETED);

        Transaction transferIn = new Transaction("txn-2", "ACC-67890", "ACC-12345",
                new BigDecimal("500.00"), "USD", TransactionType.TRANSFER,
                instant2, TransactionStatus.COMPLETED);

        Transaction withdrawal = new Transaction("txn-3", "ACC-12345", null,
                new BigDecimal("300.00"), "USD", TransactionType.WITHDRAWAL,
                instant3, TransactionStatus.COMPLETED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(deposit, transferIn, withdrawal));

        AccountSummaryResponse result = accountService.getAccountSummary("ACC-12345");

        assertThat(result.getAccountId()).isEqualTo("ACC-12345");
        assertThat(result.getTotalDeposits()).containsEntry("USD", new BigDecimal("1500.00"));
        assertThat(result.getTotalWithdrawals()).containsEntry("USD", new BigDecimal("300.00"));
        assertThat(result.getNumberOfTransactions()).isEqualTo(3);
        assertThat(result.getMostRecentTransactionDate()).isEqualTo(instant3);
    }

    @Test
    void getAccountSummary_countsAllTransactionsRegardlessOfStatus() {
        Transaction completed = new Transaction("txn-1", null, "ACC-12345",
                new BigDecimal("500.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        Transaction pending = new Transaction("txn-2", null, "ACC-12345",
                new BigDecimal("300.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.PENDING);

        Transaction failed = new Transaction("txn-3", null, "ACC-12345",
                new BigDecimal("200.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.FAILED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(completed, pending, failed));

        AccountSummaryResponse result = accountService.getAccountSummary("ACC-12345");

        assertThat(result.getNumberOfTransactions()).isEqualTo(3);
        assertThat(result.getTotalDeposits()).containsEntry("USD", new BigDecimal("1000.00"));
    }

    @Test
    void getAccountSummary_withMultipleCurrencies_separatesTotals() {
        Transaction depositUSD = new Transaction("txn-1", null, "ACC-12345",
                new BigDecimal("1000.00"), "USD", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        Transaction depositEUR = new Transaction("txn-2", null, "ACC-12345",
                new BigDecimal("500.00"), "EUR", TransactionType.DEPOSIT,
                Instant.now(), TransactionStatus.COMPLETED);

        Transaction withdrawalUSD = new Transaction("txn-3", "ACC-12345", null,
                new BigDecimal("200.00"), "USD", TransactionType.WITHDRAWAL,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(depositUSD, depositEUR, withdrawalUSD));

        AccountSummaryResponse result = accountService.getAccountSummary("ACC-12345");

        assertThat(result.getTotalDeposits()).containsEntry("USD", new BigDecimal("1000.00"));
        assertThat(result.getTotalDeposits()).containsEntry("EUR", new BigDecimal("500.00"));
        assertThat(result.getTotalWithdrawals()).containsEntry("USD", new BigDecimal("200.00"));
        assertThat(result.getTotalWithdrawals()).doesNotContainKey("EUR");
    }

    @Test
    void getAccountSummary_findsMostRecentTransaction() {
        Instant oldest = Instant.parse("2024-01-01T10:00:00Z");
        Instant middle = Instant.parse("2024-01-15T10:00:00Z");
        Instant newest = Instant.parse("2024-01-30T10:00:00Z");

        Transaction txn1 = new Transaction("txn-1", null, "ACC-12345",
                new BigDecimal("100.00"), "USD", TransactionType.DEPOSIT,
                middle, TransactionStatus.COMPLETED);

        Transaction txn2 = new Transaction("txn-2", null, "ACC-12345",
                new BigDecimal("200.00"), "USD", TransactionType.DEPOSIT,
                oldest, TransactionStatus.COMPLETED);

        Transaction txn3 = new Transaction("txn-3", null, "ACC-12345",
                new BigDecimal("300.00"), "USD", TransactionType.DEPOSIT,
                newest, TransactionStatus.COMPLETED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(txn1, txn2, txn3));

        AccountSummaryResponse result = accountService.getAccountSummary("ACC-12345");

        assertThat(result.getMostRecentTransactionDate()).isEqualTo(newest);
    }

    @Test
    void getAccountSummary_withNoTransactions_returnsZeroValues() {
        when(transactionRepository.findByAccountId("ACC-99999"))
                .thenReturn(Collections.emptyList());

        AccountSummaryResponse result = accountService.getAccountSummary("ACC-99999");

        assertThat(result.getAccountId()).isEqualTo("ACC-99999");
        assertThat(result.getNumberOfTransactions()).isEqualTo(0);
        assertThat(result.getTotalDeposits()).isEmpty();
        assertThat(result.getTotalWithdrawals()).isEmpty();
        assertThat(result.getMostRecentTransactionDate()).isNull();
    }

    @Test
    void getAccountSummary_transferOut_countsAsWithdrawal() {
        Transaction transferOut = new Transaction("txn-1", "ACC-12345", "ACC-67890",
                new BigDecimal("500.00"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(transferOut));

        AccountSummaryResponse result = accountService.getAccountSummary("ACC-12345");

        assertThat(result.getTotalWithdrawals()).containsEntry("USD", new BigDecimal("500.00"));
        assertThat(result.getTotalDeposits()).isEmpty();
    }

    @Test
    void getAccountSummary_transferIn_countsAsDeposit() {
        Transaction transferIn = new Transaction("txn-1", "ACC-67890", "ACC-12345",
                new BigDecimal("500.00"), "USD", TransactionType.TRANSFER,
                Instant.now(), TransactionStatus.COMPLETED);

        when(transactionRepository.findByAccountId("ACC-12345"))
                .thenReturn(Arrays.asList(transferIn));

        AccountSummaryResponse result = accountService.getAccountSummary("ACC-12345");

        assertThat(result.getTotalDeposits()).containsEntry("USD", new BigDecimal("500.00"));
        assertThat(result.getTotalWithdrawals()).isEmpty();
    }
}
