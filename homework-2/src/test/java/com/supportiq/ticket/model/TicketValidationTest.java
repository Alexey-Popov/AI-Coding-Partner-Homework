package com.supportiq.ticket.model;

import com.supportiq.ticket.dto.request.CreateTicketRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TicketValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validRequest_noViolations() {
        CreateTicketRequest request = validRequest();
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankCustomerName_violation() {
        CreateTicketRequest request = validRequest();
        request.setCustomerName("");
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("customerName"));
    }

    @Test
    void nullCustomerName_violation() {
        CreateTicketRequest request = validRequest();
        request.setCustomerName(null);
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void invalidEmail_violation() {
        CreateTicketRequest request = validRequest();
        request.setCustomerEmail("not-an-email");
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("customerEmail"));
    }

    @Test
    void blankEmail_violation() {
        CreateTicketRequest request = validRequest();
        request.setCustomerEmail("");
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void blankSubject_violation() {
        CreateTicketRequest request = validRequest();
        request.setSubject("");
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("subject"));
    }

    @Test
    void blankDescription_violation() {
        CreateTicketRequest request = validRequest();
        request.setDescription("");
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("description"));
    }

    @Test
    void subjectTooLong_violation() {
        CreateTicketRequest request = validRequest();
        request.setSubject("x".repeat(201));
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("subject"));
    }

    @Test
    void descriptionTooLong_violation() {
        CreateTicketRequest request = validRequest();
        request.setDescription("x".repeat(2001));
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("description"));
    }

    @Test
    void descriptionTooShort_violation() {
        CreateTicketRequest request = validRequest();
        request.setDescription("short");
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("description"));
    }

    private CreateTicketRequest validRequest() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setSubject("Test subject");
        request.setDescription("Test description");
        return request;
    }
}
