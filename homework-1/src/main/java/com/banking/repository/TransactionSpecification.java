package com.banking.repository;

import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class TransactionSpecification {

    public static Specification<Transaction> hasAccountId(String accountId) {
        return (root, query, cb) -> cb.or(
            cb.equal(root.get("fromAccount"), accountId),
            cb.equal(root.get("toAccount"), accountId)
        );
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> timestampFrom(LocalDate from) {
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), fromInstant);
    }

    public static Specification<Transaction> timestampTo(LocalDate to) {
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return (root, query, cb) -> cb.lessThan(root.get("timestamp"), toInstant);
    }
}
