package com.banking.api.service;

import com.banking.api.model.Transaction;
import com.banking.api.util.CsvUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for exporting transactions in various formats
 */
@Service
@RequiredArgsConstructor
public class ExportService {

    /**
     * Generate CSV export from list of transactions
     *
     * @param transactions List of transactions to export
     * @return CSV formatted string
     */
    public String generateCsv(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            // Return header only if no transactions
            return "Transaction ID,From Account,To Account,Amount,Currency,Type,Status,Timestamp\n";
        }
        
        return CsvUtil.transactionsToCsv(transactions);
    }

    /**
     * Get filename for CSV export with current date
     *
     * @return Filename string
     */
    public String generateCsvFilename() {
        return "transactions_" + java.time.LocalDate.now().toString() + ".csv";
    }
}
