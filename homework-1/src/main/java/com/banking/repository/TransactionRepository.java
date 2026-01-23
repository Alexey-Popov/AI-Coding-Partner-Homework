package com.banking.repository;

import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT COALESCE(SUM(CASE " +
           "WHEN t.type = 'DEPOSIT' AND t.toAccount = :accountId THEN t.amount " +
           "WHEN t.type = 'WITHDRAWAL' AND t.fromAccount = :accountId THEN -t.amount " +
           "WHEN t.type = 'TRANSFER' AND t.toAccount = :accountId THEN t.amount " +
           "WHEN t.type = 'TRANSFER' AND t.fromAccount = :accountId THEN -t.amount " +
           "ELSE 0 END), 0) FROM Transaction t " +
           "WHERE t.status = 'COMPLETED' AND (t.fromAccount = :accountId OR t.toAccount = :accountId)")
    BigDecimal calculateBalance(@Param("accountId") String accountId);
}
