package com.support.ticket.model;

import com.support.ticket.dto.CreateTicketRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TicketModelTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequestPassesValidation() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("CUST-001");
        request.setCustomerEmail("customer@example.com");
        request.setCustomerName("John Doe");
        request.setSubject("Valid Subject");
        request.setDescription("This is a valid description with more than 10 characters");
        request.setCategory(Category.TECHNICAL_ISSUE);
        request.setPriority(Priority.HIGH);
        request.setStatus(Status.NEW);

        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "Valid request should pass validation");
    }

    @Test
    void testBlankSubjectFails() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("CUST-001");
        request.setCustomerEmail("customer@example.com");
        request.setCustomerName("John Doe");
        request.setSubject("");
        request.setDescription("This is a valid description with more than 10 characters");

        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Blank subject should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("subject")),
                "Violation should be for subject field");
    }

    @Test
    void testSubjectTooLongFails() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("CUST-001");
        request.setCustomerEmail("customer@example.com");
        request.setCustomerName("John Doe");
        // Create a subject with 201 characters (exceeds max of 200)
        request.setSubject("a".repeat(201));
        request.setDescription("This is a valid description with more than 10 characters");

        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Subject exceeding 200 characters should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("subject")),
                "Violation should be for subject field");
    }

    @Test
    void testDescriptionTooShortFails() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("CUST-001");
        request.setCustomerEmail("customer@example.com");
        request.setCustomerName("John Doe");
        request.setSubject("Valid Subject");
        // Create a description with 9 characters (less than min of 10)
        request.setDescription("123456789");

        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Description with less than 10 characters should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")),
                "Violation should be for description field");
    }

    @Test
    void testDescriptionTooLongFails() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("CUST-001");
        request.setCustomerEmail("customer@example.com");
        request.setCustomerName("John Doe");
        request.setSubject("Valid Subject");
        // Create a description with 2001 characters (exceeds max of 2000)
        request.setDescription("a".repeat(2001));

        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Description exceeding 2000 characters should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")),
                "Violation should be for description field");
    }

    @Test
    void testInvalidEmailFails() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("CUST-001");
        request.setCustomerEmail("invalid-email");
        request.setCustomerName("John Doe");
        request.setSubject("Valid Subject");
        request.setDescription("This is a valid description with more than 10 characters");

        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Invalid email should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("customerEmail")),
                "Violation should be for customerEmail field");
    }

    @Test
    void testInvalidCategoryThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Category.fromValue("invalid_category"),
                "Invalid category should throw IllegalArgumentException"
        );

        assertTrue(exception.getMessage().contains("Invalid category"),
                "Exception message should indicate invalid category");
    }

    @Test
    void testInvalidPriorityThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Priority.fromValue("invalid_priority"),
                "Invalid priority should throw IllegalArgumentException"
        );

        assertTrue(exception.getMessage().contains("Invalid priority"),
                "Exception message should indicate invalid priority");
    }

    @Test
    void testInvalidStatusThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Status.fromValue("invalid_status"),
                "Invalid status should throw IllegalArgumentException"
        );

        assertTrue(exception.getMessage().contains("Invalid status"),
                "Exception message should indicate invalid status");
    }
}
