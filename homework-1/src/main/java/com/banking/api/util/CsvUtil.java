package com.banking.api.util;

import com.banking.api.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for CSV generation
 */
public class CsvUtil {

    private static final String CSV_SEPARATOR = ",";
    private static final String CSV_HEADER = "Transaction ID,From Account,To Account,Amount,Currency,Type,Status,Timestamp";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convert a list of transactions to CSV format
     *
     * @param transactions List of transactions
     * @return CSV string with header and data rows
     */
    public static String transactionsToCsv(List<Transaction> transactions) {
        StringBuilder csv = new StringBuilder();
        
        // Add header
        csv.append(CSV_HEADER).append("\n");
        
        // Add data rows
        for (Transaction transaction : transactions) {
            csv.append(transactionToCsvRow(transaction)).append("\n");
        }
        
        return csv.toString();
    }

    /**
     * Convert a single transaction to CSV row
     *
     * @param transaction Transaction object
     * @return CSV row string
     */
    private static String transactionToCsvRow(Transaction transaction) {
        return String.join(CSV_SEPARATOR,
                escapeCsvValue(transaction.getId()),
                escapeCsvValue(transaction.getFromAccount()),
                escapeCsvValue(transaction.getToAccount()),
                formatAmount(transaction.getAmount()),
                escapeCsvValue(transaction.getCurrency()),
                escapeCsvValue(transaction.getType() != null ? transaction.getType().name() : ""),
                escapeCsvValue(transaction.getStatus() != null ? transaction.getStatus().name() : ""),
                formatDateTime(transaction.getTimestamp())
        );
    }

    /**
     * Escape special characters in CSV values
     * Handles commas, quotes, and newlines
     *
     * @param value String value
     * @return Escaped value
     */
    private static String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    /**
     * Format BigDecimal amount for CSV
     *
     * @param amount Amount value
     * @return Formatted string
     */
    private static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.toString();
    }

    /**
     * Format LocalDateTime for CSV
     *
     * @param dateTime DateTime value
     * @return Formatted string in ISO format
     */
    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_FORMATTER);
    }
}
