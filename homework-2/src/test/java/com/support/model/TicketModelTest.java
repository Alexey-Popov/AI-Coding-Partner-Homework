package com.support.model;

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
    void testValidTicket() {
        Ticket ticket = createValidTicket();
        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testInvalidEmail() {
        Ticket ticket = createValidTicket();
        ticket.setCustomerEmail("invalid-email");
        
        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("email")));
    }
    
    @Test
    void testSubjectTooLong() {
        Ticket ticket = createValidTicket();
        ticket.setSubject("a".repeat(201));
        
        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testDescriptionTooShort() {
        Ticket ticket = createValidTicket();
        ticket.setDescription("short");
        
        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testDescriptionTooLong() {
        Ticket ticket = createValidTicket();
        ticket.setDescription("a".repeat(2001));
        
        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testMissingCustomerId() {
        Ticket ticket = createValidTicket();
        ticket.setCustomerId(null);
        
        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testMissingCustomerName() {
        Ticket ticket = createValidTicket();
        ticket.setCustomerName("");
        
        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testPrePersist_GeneratesId() {
        Ticket ticket = createValidTicket();
        ticket.setId(null);
        ticket.prePersist();
        
        assertNotNull(ticket.getId());
    }
    
    @Test
    void testPrePersist_DefaultValues() {
        Ticket ticket = createValidTicket();
        ticket.setCategory(null);
        ticket.setPriority(null);
        ticket.prePersist();
        
        assertEquals(Ticket.Category.OTHER, ticket.getCategory());
        assertEquals(Ticket.Priority.MEDIUM, ticket.getPriority());
    }
    
    private Ticket createValidTicket() {
        return Ticket.builder()
            .id("test-123")
            .customerId("CUST-001")
            .customerEmail("test@example.com")
            .customerName("Test Customer")
            .subject("Test Issue")
            .description("This is a test description with more than 10 characters")
            .category(Ticket.Category.TECHNICAL_ISSUE)
            .priority(Ticket.Priority.MEDIUM)
            .status(Ticket.Status.NEW)
            .build();
    }
}
