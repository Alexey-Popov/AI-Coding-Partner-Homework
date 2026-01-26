package com.banking.api.controller;

import com.banking.api.model.Transaction;
import com.banking.api.service.ExportService;
import com.banking.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Export operations
 * Handles exporting transactions in various formats
 */
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class ExportController {

    private final TransactionService transactionService;
    private final ExportService exportService;

    /**
     * Export transactions in specified format
     * GET /api/v1/transactions/export?format=csv
     * 
     * Supports optional filtering parameters (same as GET /transactions):
     * - accountId: Filter by account
     * - type: Filter by transaction type
     * - from: Filter by start date
     * - to: Filter by end date
     * - status: Filter by status
     *
     * @param format Export format (must be "csv")
     * @param accountId Optional account filter
     * @param type Optional type filter
     * @param from Optional start date filter
     * @param to Optional end date filter
     * @param status Optional status filter
     * @return CSV file download or error response
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportTransactions(
            @RequestParam String format,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String status) {
        
        // Validate format
        if (!"csv".equalsIgnoreCase(format)) {
            return ResponseEntity
                    .badRequest()
                    .body("{\"error\": \"Invalid format. Only 'csv' is supported.\"}");
        }

        // Get filtered transactions (using same filtering as GET /transactions)
        List<Transaction> transactions = transactionService.filterTransactionsAsModels(
                accountId, type, from, to, status);

        // Generate CSV content
        String csvContent = exportService.generateCsv(transactions);
        
        // Generate filename
        String filename = exportService.generateCsvFilename();

        // Set response headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(csvContent);
    }
}
