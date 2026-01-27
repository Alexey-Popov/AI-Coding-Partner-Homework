package com.banking.service;

import com.banking.controller.dto.CreateTransactionRequest;
import com.banking.model.Transaction;
import com.banking.model.TransactionStatus;
import com.banking.model.TransactionType;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService(repository);
    }

    @Test
    void create_setsAllFields() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.50"));
        request.setCurrency("usd");
        request.setType("transfer");

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Transaction result = service.create(request);

        assertNotNull(result.getId());
        assertEquals("ACC-12345", result.getFromAccount());
        assertEquals("ACC-67890", result.getToAccount());
        assertEquals(new BigDecimal("100.50"), result.getAmount());
        assertEquals("USD", result.getCurrency());
        assertEquals(TransactionType.TRANSFER, result.getType());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void create_currencyUppercased() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("eur");
        request.setType("transfer");

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Transaction result = service.create(request);

        assertEquals("EUR", result.getCurrency());
    }

    @Test
    void create_generatesUniqueId() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setType("transfer");

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Transaction result1 = service.create(request);
        Transaction result2 = service.create(request);

        assertNotEquals(result1.getId(), result2.getId());
    }

    @Test
    void create_setsCompletedStatus() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setToAccount("ACC-12345");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setType("deposit");

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Transaction result = service.create(request);

        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
    }

    @Test
    void findAll_noFilters_callsRepositoryWithNullSpec() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());

        service.findAll(null, null, null, null);

        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void findAll_withAccountId_appliesFilter() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());

        service.findAll("ACC-12345", null, null, null);

        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void findAll_withType_appliesFilter() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());

        service.findAll(null, "transfer", null, null);

        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void findAll_withDateRange_appliesFilter() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());

        service.findAll(null, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void findAll_withAllFilters_appliesAllFilters() {
        when(repository.findAll(any(Specification.class))).thenReturn(List.of());

        service.findAll("ACC-12345", "deposit", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        verify(repository).findAll(any(Specification.class));
    }

    @Test
    void findById_existing_returnsTransaction() {
        UUID id = UUID.randomUUID();
        Transaction tx = new Transaction();
        tx.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(tx));

        Optional<Transaction> result = service.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findById_notFound_returnsEmpty() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Transaction> result = service.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBalance_delegatesToRepository() {
        when(repository.calculateBalance("ACC-12345")).thenReturn(new BigDecimal("500.00"));

        BigDecimal balance = service.getBalance("ACC-12345");

        assertEquals(new BigDecimal("500.00"), balance);
        verify(repository).calculateBalance("ACC-12345");
    }

    @Test
    void getBalance_noTransactions_returnsZero() {
        when(repository.calculateBalance("ACC-99999")).thenReturn(BigDecimal.ZERO);

        BigDecimal balance = service.getBalance("ACC-99999");

        assertEquals(BigDecimal.ZERO, balance);
    }
}
