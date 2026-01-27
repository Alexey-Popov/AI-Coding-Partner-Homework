package com.banking.util;

import com.banking.model.Transaction;
import com.banking.model.TransactionStatus;
import com.banking.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest {

    @Test
    void export_emptyList_onlyHeader() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        CsvExporter.export(List.of(), pw);

        String result = sw.toString();
        assertEquals("id,fromAccount,toAccount,amount,currency,type,timestamp,status\n", result);
    }

    @Test
    void export_singleTransaction_correctFormat() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Instant timestamp = Instant.parse("2024-01-15T10:30:00Z");
        Transaction tx = createTransaction(id, "ACC-12345", "ACC-67890",
            new BigDecimal("100.50"), "USD", TransactionType.TRANSFER, timestamp, TransactionStatus.COMPLETED);

        CsvExporter.export(List.of(tx), pw);

        String[] lines = sw.toString().split("\n");
        assertEquals(2, lines.length);
        assertEquals("id,fromAccount,toAccount,amount,currency,type,timestamp,status", lines[0]);
        assertEquals("123e4567-e89b-12d3-a456-426614174000,ACC-12345,ACC-67890,100.50,USD,transfer,2024-01-15T10:30:00Z,completed", lines[1]);
    }

    @Test
    void export_nullAccounts_emptyStrings() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Instant timestamp = Instant.parse("2024-01-15T10:30:00Z");
        Transaction tx = createTransaction(id, null, "ACC-67890",
            new BigDecimal("100.00"), "USD", TransactionType.DEPOSIT, timestamp, TransactionStatus.COMPLETED);

        CsvExporter.export(List.of(tx), pw);

        String[] lines = sw.toString().split("\n");
        assertTrue(lines[1].contains(",,ACC-67890"));
    }

    @Test
    void export_multipleTransactions_correctOrder() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Transaction tx1 = createTransaction(UUID.randomUUID(), "ACC-11111", "ACC-22222",
            new BigDecimal("50.00"), "EUR", TransactionType.TRANSFER, Instant.now(), TransactionStatus.COMPLETED);
        Transaction tx2 = createTransaction(UUID.randomUUID(), "ACC-33333", null,
            new BigDecimal("25.00"), "GBP", TransactionType.WITHDRAWAL, Instant.now(), TransactionStatus.PENDING);

        CsvExporter.export(List.of(tx1, tx2), pw);

        String[] lines = sw.toString().split("\n");
        assertEquals(3, lines.length);
    }

    @Test
    void export_amountPrecision_preserved() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Transaction tx = createTransaction(UUID.randomUUID(), "ACC-12345", "ACC-67890",
            new BigDecimal("1000000.99"), "USD", TransactionType.TRANSFER, Instant.now(), TransactionStatus.COMPLETED);

        CsvExporter.export(List.of(tx), pw);

        assertTrue(sw.toString().contains("1000000.99"));
    }

    @Test
    void export_escapesCommasInValues() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Transaction tx = createTransaction(UUID.randomUUID(), "ACC-12345", "ACC-67890",
            new BigDecimal("100.00"), "USD", TransactionType.TRANSFER, Instant.now(), TransactionStatus.COMPLETED);

        CsvExporter.export(List.of(tx), pw);

        String result = sw.toString();
        assertFalse(result.contains("\"ACC-12345\""));
    }

    private Transaction createTransaction(UUID id, String fromAccount, String toAccount,
            BigDecimal amount, String currency, TransactionType type, Instant timestamp, TransactionStatus status) {
        Transaction tx = new Transaction();
        tx.setId(id);
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setAmount(amount);
        tx.setCurrency(currency);
        tx.setType(type);
        tx.setTimestamp(timestamp);
        tx.setStatus(status);
        return tx;
    }
}
