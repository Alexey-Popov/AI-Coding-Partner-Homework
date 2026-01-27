package com.example.demo.controller;

import com.example.demo.model.Transaction;
import com.example.demo.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping(path = "/transactions")
    public ResponseEntity<?> createTransaction(@RequestBody Transaction request) {
        try {
            Transaction created = service.createTransaction(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Validation failed");
            error.put("details", List.of(Map.of("field", "amount", "message", ex.getMessage())));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping(path = "/transactions")
    public List<Transaction> getAllTransactions() {
        return service.getAllTransactions();
    }

    @GetMapping(path = "/transactions/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable String id) {
        return service.getTransactionById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Transaction not found")));
    }

    @GetMapping(path = "/accounts/{accountId}/balance")
    public Map<String, Object> getAccountBalance(@PathVariable String accountId) {
        BigDecimal balance = service.getAccountBalance(accountId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("accountId", accountId);
        resp.put("balance", balance);
        return resp;
    }
}
