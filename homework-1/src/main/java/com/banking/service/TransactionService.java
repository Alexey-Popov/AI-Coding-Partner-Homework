package com.banking.service;

import com.banking.controller.dto.CreateTransactionRequest;
import com.banking.model.Transaction;
import com.banking.model.TransactionStatus;
import com.banking.model.TransactionType;
import com.banking.repository.TransactionRepository;
import com.banking.repository.TransactionSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction create(CreateTransactionRequest request) {
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID());
        tx.setFromAccount(request.getFromAccount());
        tx.setToAccount(request.getToAccount());
        tx.setAmount(request.getAmount());
        tx.setCurrency(request.getCurrency().toUpperCase());
        tx.setType(TransactionType.fromString(request.getType()));
        tx.setTimestamp(Instant.now());
        tx.setStatus(TransactionStatus.COMPLETED);
        return repository.save(tx);
    }

    public List<Transaction> findAll(String accountId, String type, LocalDate from, LocalDate to) {
        Specification<Transaction> spec = Specification.allOf();

        if (accountId != null && !accountId.isBlank()) {
            spec = spec.and(TransactionSpecification.hasAccountId(accountId));
        }
        if (type != null && !type.isBlank()) {
            spec = spec.and(TransactionSpecification.hasType(TransactionType.fromString(type)));
        }
        if (from != null) {
            spec = spec.and(TransactionSpecification.timestampFrom(from));
        }
        if (to != null) {
            spec = spec.and(TransactionSpecification.timestampTo(to));
        }

        return repository.findAll(spec);
    }

    public Optional<Transaction> findById(UUID id) {
        return repository.findById(id);
    }

    public BigDecimal getBalance(String accountId) {
        return repository.calculateBalance(accountId);
    }
}
