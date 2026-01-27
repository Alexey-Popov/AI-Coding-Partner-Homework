package com.banking.util;

import com.banking.model.Transaction;

import java.io.PrintWriter;
import java.util.List;

public class CsvExporter {

    private static final String HEADER = "id,fromAccount,toAccount,amount,currency,type,timestamp,status";

    public static void export(List<Transaction> transactions, PrintWriter writer) {
        writer.println(HEADER);
        for (Transaction tx : transactions) {
            writer.println(formatRow(tx));
        }
    }

    private static String formatRow(Transaction tx) {
        return String.join(",",
            tx.getId().toString(),
            escapeCsv(tx.getFromAccount()),
            escapeCsv(tx.getToAccount()),
            tx.getAmount().toPlainString(),
            tx.getCurrency(),
            tx.getType().getValue(),
            tx.getTimestamp().toString(),
            tx.getStatus().getValue()
        );
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
