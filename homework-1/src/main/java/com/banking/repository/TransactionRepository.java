package com.banking.repository;

import com.banking.model.Transaction;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class TransactionRepository {

    private final Map<String, Transaction> store = new ConcurrentHashMap<>();

    public Transaction save(Transaction transaction) {
        store.put(transaction.getId(), transaction);
        return transaction;
    }

    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Transaction> findAll(String accountId, String type, LocalDateTime from, LocalDateTime to) {
        return store.values().stream()
                .filter(t -> accountId == null ||
                        accountId.equals(t.getFromAccount()) || accountId.equals(t.getToAccount()))
                .filter(t -> type == null || type.equals(t.getType()))
                .filter(t -> from == null || !t.getTimestamp().isBefore(from))
                .filter(t -> to == null || !t.getTimestamp().isAfter(to))
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public List<Transaction> findByAccount(String accountId) {
        return store.values().stream()
                .filter(t -> accountId.equals(t.getFromAccount()) || accountId.equals(t.getToAccount()))
                .collect(Collectors.toList());
    }
}
