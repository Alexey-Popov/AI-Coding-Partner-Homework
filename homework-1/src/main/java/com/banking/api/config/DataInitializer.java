package com.banking.api.config;

import com.banking.api.model.Transaction;
import com.banking.api.model.TransactionStatus;
import com.banking.api.model.TransactionType;
import com.banking.api.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data initializer that populates sample transactions on application startup.
 *
 * <p>This component implements {@link CommandLineRunner} to execute initialization
 * logic after the Spring context is fully loaded.
 *
 * <p><b>Sample Data Created:</b>
 * <ul>
 *   <li>Account ACC-12345: Initial deposits totaling $5,000.00</li>
 *   <li>Account ACC-67890: Initial deposits totaling $3,000.00</li>
 *   <li>Account ACC-11111: Initial deposits totaling $10,000.00</li>
 *   <li>Sample transfers and withdrawals for testing</li>
 * </ul>
 *
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final TransactionRepository transactionRepository;

    public DataInitializer(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing sample transaction data...");

        // Create sample deposits to establish account balances
        createTransaction(null, "ACC-12345", new BigDecimal("5000.00"), "USD",
                TransactionType.DEPOSIT, LocalDateTime.now().minusDays(30));

        createTransaction(null, "ACC-67890", new BigDecimal("3000.00"), "USD",
                TransactionType.DEPOSIT, LocalDateTime.now().minusDays(25));

        createTransaction(null, "ACC-11111", new BigDecimal("10000.00"), "USD",
                TransactionType.DEPOSIT, LocalDateTime.now().minusDays(20));

        // Create sample transfers
        createTransaction("ACC-12345", "ACC-67890", new BigDecimal("500.00"), "USD",
                TransactionType.TRANSFER, LocalDateTime.now().minusDays(15));

        createTransaction("ACC-11111", "ACC-12345", new BigDecimal("1000.00"), "USD",
                TransactionType.TRANSFER, LocalDateTime.now().minusDays(10));

        // Create sample withdrawal
        createTransaction("ACC-67890", null, new BigDecimal("200.00"), "USD",
                TransactionType.WITHDRAWAL, LocalDateTime.now().minusDays(5));

        // Create some EUR transactions
        createTransaction(null, "ACC-EUR01", new BigDecimal("2000.00"), "EUR",
                TransactionType.DEPOSIT, LocalDateTime.now().minusDays(7));

        createTransaction("ACC-EUR01", "ACC-12345", new BigDecimal("150.00"), "EUR",
                TransactionType.TRANSFER, LocalDateTime.now().minusDays(3));

        log.info("Sample data initialization complete. Created {} transactions.",
                transactionRepository.count());

        // Log account summaries
        log.info("Sample accounts initialized:");
        log.info("  - ACC-12345: Deposits $5,000 + Incoming transfers $1,000");
        log.info("  - ACC-67890: Deposits $3,000 + Incoming transfers $500 - Withdrawals $200");
        log.info("  - ACC-11111: Deposits $10,000 - Outgoing transfers $1,000");
        log.info("  - ACC-EUR01: Deposits EUR 2,000 - Outgoing transfers EUR 150");
    }

    private void createTransaction(String fromAccount, String toAccount,
                                   BigDecimal amount, String currency,
                                   TransactionType type, LocalDateTime timestamp) {
        Transaction transaction = Transaction.builder()
                .id(Transaction.generateId())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .currency(currency)
                .type(type)
                .timestamp(timestamp)
                .status(TransactionStatus.COMPLETED)
                .build();

        transactionRepository.save(transaction);
        log.debug("Created sample transaction: {} {} {}", type, amount, currency);
    }
}
