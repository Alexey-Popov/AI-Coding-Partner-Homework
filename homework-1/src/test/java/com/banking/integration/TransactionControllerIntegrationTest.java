package com.banking.integration;

import com.banking.controller.dto.BalanceResponse;
import com.banking.controller.dto.CreateTransactionRequest;
import com.banking.controller.dto.ValidationErrorResponse;
import com.banking.model.Transaction;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createTransaction_validTransfer_returns201() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.50"));
        request.setCurrency("USD");
        request.setType("transfer");

        ResponseEntity<Transaction> response = restTemplate.postForEntity(
            "/transactions", request, Transaction.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("ACC-12345", response.getBody().getFromAccount());
        assertEquals("ACC-67890", response.getBody().getToAccount());
        assertEquals(new BigDecimal("100.50"), response.getBody().getAmount());
        assertEquals("USD", response.getBody().getCurrency());
        assertEquals("transfer", response.getBody().getType().getValue());
        assertEquals("completed", response.getBody().getStatus().getValue());
    }

    @Test
    void createTransaction_validDeposit_returns201() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setToAccount("ACC-12345");
        request.setAmount(new BigDecimal("500.00"));
        request.setCurrency("EUR");
        request.setType("deposit");

        ResponseEntity<Transaction> response = restTemplate.postForEntity(
            "/transactions", request, Transaction.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getFromAccount());
        assertEquals("ACC-12345", response.getBody().getToAccount());
    }

    @Test
    void createTransaction_validWithdrawal_returns201() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency("GBP");
        request.setType("withdrawal");

        ResponseEntity<Transaction> response = restTemplate.postForEntity(
            "/transactions", request, Transaction.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACC-12345", response.getBody().getFromAccount());
        assertNull(response.getBody().getToAccount());
    }

    @Test
    void createTransaction_invalidAmount_returns400() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("-100.00"));
        request.setCurrency("USD");
        request.setType("transfer");

        ResponseEntity<ValidationErrorResponse> response = restTemplate.postForEntity(
            "/transactions", request, ValidationErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getError());
        assertTrue(response.getBody().getDetails().stream()
            .anyMatch(e -> e.getField().equals("amount")));
    }

    @Test
    void createTransaction_invalidCurrency_returns400() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("INVALID");
        request.setType("transfer");

        ResponseEntity<ValidationErrorResponse> response = restTemplate.postForEntity(
            "/transactions", request, ValidationErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getDetails().stream()
            .anyMatch(e -> e.getField().equals("currency")));
    }

    @Test
    void createTransaction_invalidAccountFormat_returns400() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("INVALID");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setType("transfer");

        ResponseEntity<ValidationErrorResponse> response = restTemplate.postForEntity(
            "/transactions", request, ValidationErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getDetails().stream()
            .anyMatch(e -> e.getField().equals("fromAccount")));
    }

    @Test
    void listTransactions_empty_returnsEmptyList() {
        ResponseEntity<List<Transaction>> response = restTemplate.exchange(
            "/transactions",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void listTransactions_withData_returnsAll() {
        createTestTransaction("ACC-11111", "ACC-22222", "100.00", "transfer");
        createTestTransaction("ACC-33333", "ACC-44444", "200.00", "transfer");

        ResponseEntity<List<Transaction>> response = restTemplate.exchange(
            "/transactions",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void listTransactions_filterByAccountId_returnsMatching() {
        createTestTransaction("ACC-11111", "ACC-22222", "100.00", "transfer");
        createTestTransaction("ACC-33333", "ACC-44444", "200.00", "transfer");

        ResponseEntity<List<Transaction>> response = restTemplate.exchange(
            "/transactions?accountId=ACC-11111",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("ACC-11111", response.getBody().get(0).getFromAccount());
    }

    @Test
    void listTransactions_filterByType_returnsMatching() {
        createTestDeposit("ACC-11111", "500.00");
        createTestTransaction("ACC-22222", "ACC-33333", "100.00", "transfer");

        ResponseEntity<List<Transaction>> response = restTemplate.exchange(
            "/transactions?type=deposit",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("deposit", response.getBody().get(0).getType().getValue());
    }

    @Test
    void getTransactionById_exists_returnsTransaction() {
        Transaction created = createTestTransaction("ACC-11111", "ACC-22222", "100.00", "transfer");

        ResponseEntity<Transaction> response = restTemplate.getForEntity(
            "/transactions/" + created.getId(), Transaction.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(created.getId(), response.getBody().getId());
    }

    @Test
    void getTransactionById_notFound_returns404() {
        ResponseEntity<Void> response = restTemplate.getForEntity(
            "/transactions/00000000-0000-0000-0000-000000000000", Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getBalance_noTransactions_returnsZero() {
        ResponseEntity<BalanceResponse> response = restTemplate.getForEntity(
            "/accounts/ACC-12345/balance", BalanceResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ACC-12345", response.getBody().getAccountId());
        assertEquals(0, response.getBody().getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void getBalance_withDeposit_returnsPositive() {
        createTestDeposit("ACC-12345", "500.00");

        ResponseEntity<BalanceResponse> response = restTemplate.getForEntity(
            "/accounts/ACC-12345/balance", BalanceResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, new BigDecimal("500.00").compareTo(response.getBody().getBalance()));
    }

    @Test
    void getBalance_withWithdrawal_returnsNegative() {
        createTestWithdrawal("ACC-12345", "100.00");

        ResponseEntity<BalanceResponse> response = restTemplate.getForEntity(
            "/accounts/ACC-12345/balance", BalanceResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, new BigDecimal("-100.00").compareTo(response.getBody().getBalance()));
    }

    @Test
    void getBalance_withTransfers_calculatesCorrectly() {
        createTestDeposit("ACC-12345", "1000.00");
        createTestTransaction("ACC-12345", "ACC-67890", "300.00", "transfer");
        createTestTransaction("ACC-99999", "ACC-12345", "150.00", "transfer");

        ResponseEntity<BalanceResponse> response = restTemplate.getForEntity(
            "/accounts/ACC-12345/balance", BalanceResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, new BigDecimal("850.00").compareTo(response.getBody().getBalance()));
    }

    @Test
    void exportCsv_returnsValidCsv() {
        createTestTransaction("ACC-11111", "ACC-22222", "100.00", "transfer");

        ResponseEntity<String> response = restTemplate.getForEntity(
            "/transactions/export?format=csv", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString().contains("text/csv"));
        String body = response.getBody();
        assertTrue(body.startsWith("id,fromAccount,toAccount,amount,currency,type,timestamp,status"));
        assertTrue(body.contains("ACC-11111"));
        assertTrue(body.contains("ACC-22222"));
        assertTrue(body.contains("100.00"));
    }

    @Test
    void exportCsv_withFilter_returnsFilteredData() {
        createTestTransaction("ACC-11111", "ACC-22222", "100.00", "transfer");
        createTestTransaction("ACC-33333", "ACC-44444", "200.00", "transfer");

        ResponseEntity<String> response = restTemplate.getForEntity(
            "/transactions/export?format=csv&accountId=ACC-11111", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertTrue(body.contains("ACC-11111"));
        assertFalse(body.contains("ACC-33333"));
    }

    private Transaction createTestTransaction(String from, String to, String amount, String type) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount(from);
        request.setToAccount(to);
        request.setAmount(new BigDecimal(amount));
        request.setCurrency("USD");
        request.setType(type);
        return restTemplate.postForEntity("/transactions", request, Transaction.class).getBody();
    }

    private Transaction createTestDeposit(String toAccount, String amount) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setToAccount(toAccount);
        request.setAmount(new BigDecimal(amount));
        request.setCurrency("USD");
        request.setType("deposit");
        return restTemplate.postForEntity("/transactions", request, Transaction.class).getBody();
    }

    private Transaction createTestWithdrawal(String fromAccount, String amount) {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount(fromAccount);
        request.setAmount(new BigDecimal(amount));
        request.setCurrency("USD");
        request.setType("withdrawal");
        return restTemplate.postForEntity("/transactions", request, Transaction.class).getBody();
    }
}
