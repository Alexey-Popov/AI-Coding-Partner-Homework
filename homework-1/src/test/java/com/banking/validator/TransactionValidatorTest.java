package com.banking.validator;

import com.banking.controller.dto.CreateTransactionRequest;
import com.banking.controller.dto.ValidationErrorResponse.FieldError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionValidatorTest {

    private TransactionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TransactionValidator();
    }

    @Test
    void validTransfer_noErrors() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.50"));
        request.setCurrency("USD");
        request.setType("transfer");

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validDeposit_noErrors() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setToAccount("ACC-12345");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("EUR");
        request.setType("deposit");

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validWithdrawal_noErrors() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency("GBP");
        request.setType("withdrawal");

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void nullAmount_returnsError() {
        CreateTransactionRequest request = createValidTransfer();
        request.setAmount(null);

        List<FieldError> errors = validator.validate(request);

        assertEquals(1, errors.size());
        assertEquals("amount", errors.get(0).getField());
        assertEquals("Amount is required", errors.get(0).getMessage());
    }

    @Test
    void negativeAmount_returnsError() {
        CreateTransactionRequest request = createValidTransfer();
        request.setAmount(new BigDecimal("-100.00"));

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e ->
            e.getField().equals("amount") && e.getMessage().contains("positive")));
    }

    @Test
    void zeroAmount_returnsError() {
        CreateTransactionRequest request = createValidTransfer();
        request.setAmount(BigDecimal.ZERO);

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e ->
            e.getField().equals("amount") && e.getMessage().contains("positive")));
    }

    @Test
    void amountWithMoreThanTwoDecimals_returnsError() {
        CreateTransactionRequest request = createValidTransfer();
        request.setAmount(new BigDecimal("100.123"));

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e ->
            e.getField().equals("amount") && e.getMessage().contains("2 decimal")));
    }

    @Test
    void amountWithTwoDecimals_noError() {
        CreateTransactionRequest request = createValidTransfer();
        request.setAmount(new BigDecimal("100.99"));

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void nullCurrency_returnsError() {
        CreateTransactionRequest request = createValidTransfer();
        request.setCurrency(null);

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e ->
            e.getField().equals("currency") && e.getMessage().contains("required")));
    }

    @Test
    void invalidCurrency_returnsError() {
        CreateTransactionRequest request = createValidTransfer();
        request.setCurrency("ZZZ");

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e ->
            e.getField().equals("currency") && e.getMessage().contains("Invalid")));
    }

    @Test
    void validCurrencyCodes_noError() {
        String[] validCodes = {"USD", "EUR", "GBP", "JPY", "usd"};

        for (String code : validCodes) {
            CreateTransactionRequest request = createValidTransfer();
            request.setCurrency(code);
            List<FieldError> errors = validator.validate(request);
            assertTrue(errors.isEmpty(), "Expected no error for currency: " + code);
        }
    }

    @Test
    void nullType_returnsError() {
        CreateTransactionRequest request = createValidTransfer();
        request.setType(null);

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e ->
            e.getField().equals("type") && e.getMessage().contains("required")));
    }

    @Test
    void invalidType_returnsError() {
        CreateTransactionRequest request = createValidTransfer();
        request.setType("invalid");

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e ->
            e.getField().equals("type") && e.getMessage().contains("deposit, withdrawal, or transfer")));
    }

    @Test
    void invalidAccountFormat_returnsError() {
        CreateTransactionRequest request = createValidTransfer();
        request.setFromAccount("INVALID");

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e ->
            e.getField().equals("fromAccount") && e.getMessage().contains("ACC-XXXXX")));
    }

    @Test
    void accountFormatVariations() {
        String[] validAccounts = {"ACC-12345", "ACC-ABCDE", "ACC-1a2B3"};
        String[] invalidAccounts = {"ACC12345", "ACC-1234", "ACC-123456", "acc-12345", "ACC-1234!"};

        for (String account : validAccounts) {
            CreateTransactionRequest request = createValidTransfer();
            request.setFromAccount(account);
            request.setToAccount("ACC-99999");
            List<FieldError> errors = validator.validate(request);
            assertTrue(errors.isEmpty(), "Expected valid: " + account);
        }

        for (String account : invalidAccounts) {
            CreateTransactionRequest request = createValidTransfer();
            request.setFromAccount(account);
            List<FieldError> errors = validator.validate(request);
            assertFalse(errors.isEmpty(), "Expected invalid: " + account);
        }
    }

    @Test
    void deposit_missingToAccount_returnsError() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setType("deposit");

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e ->
            e.getField().equals("toAccount") && e.getMessage().contains("required")));
    }

    @Test
    void withdrawal_missingFromAccount_returnsError() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setType("withdrawal");

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e ->
            e.getField().equals("fromAccount") && e.getMessage().contains("required")));
    }

    @Test
    void transfer_missingBothAccounts_returnsTwoErrors() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setType("transfer");

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.stream().anyMatch(e -> e.getField().equals("fromAccount")));
        assertTrue(errors.stream().anyMatch(e -> e.getField().equals("toAccount")));
    }

    @Test
    void multipleErrors_allReturned() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("-100.123"));
        request.setCurrency("INVALID");
        request.setType("invalid");

        List<FieldError> errors = validator.validate(request);

        assertTrue(errors.size() >= 3);
    }

    private CreateTransactionRequest createValidTransfer() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setFromAccount("ACC-12345");
        request.setToAccount("ACC-67890");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setType("transfer");
        return request;
    }
}
