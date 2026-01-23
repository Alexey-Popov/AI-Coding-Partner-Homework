package com.banking.controller;

import com.banking.controller.dto.BalanceResponse;
import com.banking.controller.dto.CreateTransactionRequest;
import com.banking.controller.dto.ValidationErrorResponse;
import com.banking.controller.dto.ValidationErrorResponse.FieldError;
import com.banking.model.Transaction;
import com.banking.service.TransactionService;
import com.banking.util.CsvExporter;
import com.banking.validator.TransactionValidator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
public class TransactionController {

    private final TransactionService service;
    private final TransactionValidator validator;

    public TransactionController(TransactionService service, TransactionValidator validator) {
        this.service = service;
        this.validator = validator;
    }

    @PostMapping("/transactions")
    public ResponseEntity<?> create(@RequestBody CreateTransactionRequest request) {
        List<FieldError> errors = validator.validate(request);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse("Validation failed", errors));
        }
        Transaction tx = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @GetMapping("/transactions")
    public List<Transaction> list(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.findAll(accountId, type, from, to);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable UUID id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse getBalance(@PathVariable String accountId) {
        return new BalanceResponse(accountId, service.getBalance(accountId));
    }

    @GetMapping("/transactions/export")
    public void exportCsv(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"transactions.csv\"");

        List<Transaction> transactions = service.findAll(accountId, type, from, to);
        transactions.sort(Comparator.comparing(Transaction::getTimestamp));

        CsvExporter.export(transactions, response.getWriter());
    }
}
