package com.example.banking.service;

import com.example.banking.dto.CreateTransactionRequest;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionStatus;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(CreateTransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setFromAccount(request.getFromAccount());
        transaction.setToAccount(request.getToAccount());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency().toUpperCase());
        transaction.setType(request.getType());
        transaction.setTimestamp(Instant.now());
        transaction.setStatus(TransactionStatus.COMPLETED);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactions(String accountId, String type, String from, String to) {
        List<Transaction> transactions = transactionRepository.findAll();

        // Filter by accountId
        if (accountId != null && !accountId.isBlank()) {
            transactions = transactions.stream()
                    .filter(t -> accountId.equals(t.getFromAccount()) || accountId.equals(t.getToAccount()))
                    .collect(Collectors.toList());
        }

        // Filter by type
        if (type != null && !type.isBlank()) {
            try {
                TransactionType transactionType = TransactionType.fromValue(type);
                transactions = transactions.stream()
                        .filter(t -> t.getType() == transactionType)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid transaction type: " + type);
            }
        }

        // Filter by date range
        if (from != null && !from.isBlank()) {
            try {
                Instant fromDate = LocalDate.parse(from).atStartOfDay().toInstant(ZoneOffset.UTC);
                transactions = transactions.stream()
                        .filter(t -> !t.getTimestamp().isBefore(fromDate))
                        .collect(Collectors.toList());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid 'from' date format. Expected: YYYY-MM-DD");
            }
        }

        if (to != null && !to.isBlank()) {
            try {
                Instant toDate = LocalDate.parse(to).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                transactions = transactions.stream()
                        .filter(t -> t.getTimestamp().isBefore(toDate))
                        .collect(Collectors.toList());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid 'to' date format. Expected: YYYY-MM-DD");
            }
        }

        return transactions;
    }

    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + id));
    }
}
