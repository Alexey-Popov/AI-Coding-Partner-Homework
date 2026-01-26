package com.banking.api.controller;

import com.banking.api.dto.TransactionRequest;
import com.banking.api.dto.TransactionResponse;
import com.banking.api.model.TransactionType;
import com.banking.api.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        
        List<TransactionResponse> transactions;
        
        if (accountId != null || type != null || from != null || to != null) {
            transactions = transactionService.getFilteredTransactions(accountId, type, from, to);
        } else {
            transactions = transactionService.getAllTransactions();
        }
        
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable String id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }
}
