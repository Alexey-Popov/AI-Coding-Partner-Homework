package com.support.ticket.service;

import com.support.ticket.dto.ClassificationResult;
import com.support.ticket.model.Category;
import com.support.ticket.model.Priority;
import com.support.ticket.model.Ticket;
import com.support.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class CategorizationTest {

    private ClassificationService classificationService;
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository = Mockito.mock(TicketRepository.class);
        classificationService = new ClassificationService(ticketRepository);
    }

    @Test
    void testAccountAccessKeywords() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setSubject("Can't log in");
        ticket.setDescription("I'm locked out of my account, password reset not working, authentication fails");

        // When
        ClassificationResult result = classificationService.classify(ticket);

        // Then
        assertEquals(Category.ACCOUNT_ACCESS, result.getCategory());
        assertNotNull(result.getPriority());
        assertTrue(result.getConfidence() > 0);
        assertNotNull(result.getReasoning());
        assertNotNull(result.getKeywordsFound());
    }

    @Test
    void testTechnicalIssueKeywords() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setSubject("App not working");
        ticket.setDescription("The application crashes with an error every time I try to use it, very slow and unresponsive");

        // When
        ClassificationResult result = classificationService.classify(ticket);

        // Then
        assertEquals(Category.TECHNICAL_ISSUE, result.getCategory());
        assertNotNull(result.getPriority());
        assertTrue(result.getConfidence() > 0);
        assertNotNull(result.getReasoning());
        assertNotNull(result.getKeywordsFound());
    }

    @Test
    void testBillingKeywords() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setSubject("Payment issue");
        ticket.setDescription("I was charged incorrectly on my invoice, need a refund for the billing error on my subscription");

        // When
        ClassificationResult result = classificationService.classify(ticket);

        // Then
        assertEquals(Category.BILLING_QUESTION, result.getCategory());
        assertNotNull(result.getPriority());
        assertTrue(result.getConfidence() > 0);
        assertNotNull(result.getReasoning());
        assertNotNull(result.getKeywordsFound());
    }

    @Test
    void testFeatureRequestKeywords() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setSubject("Feature suggestion");
        ticket.setDescription("It would be nice if you could add support for dark mode, this enhancement would improve the experience");

        // When
        ClassificationResult result = classificationService.classify(ticket);

        // Then
        assertEquals(Category.FEATURE_REQUEST, result.getCategory());
        assertNotNull(result.getPriority());
        assertTrue(result.getConfidence() > 0);
        assertNotNull(result.getReasoning());
        assertNotNull(result.getKeywordsFound());
    }

    @Test
    void testBugReportKeywords() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setSubject("Bug found in checkout");
        ticket.setDescription("Steps to reproduce: 1. Go to checkout 2. Click pay. Expected: payment processes. Actual: error shown. This is a defect.");

        // When
        ClassificationResult result = classificationService.classify(ticket);

        // Then
        assertEquals(Category.BUG_REPORT, result.getCategory());
        assertNotNull(result.getPriority());
        assertTrue(result.getConfidence() > 0);
        assertNotNull(result.getReasoning());
        assertNotNull(result.getKeywordsFound());
    }

    @Test
    void testNoKeywords() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setSubject("Hello");
        ticket.setDescription("I have a general question about your services that I'd like to discuss");

        // When
        ClassificationResult result = classificationService.classify(ticket);

        // Then
        assertEquals(Category.OTHER, result.getCategory());
        assertNotNull(result.getPriority());
        assertNotNull(result.getReasoning());
        assertNotNull(result.getKeywordsFound());
    }

    @Test
    void testUrgentPriority() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setSubject("Critical security issue");
        ticket.setDescription("Production down, there is a data loss emergency happening right now");

        // When
        ClassificationResult result = classificationService.classify(ticket);

        // Then
        assertEquals(Priority.URGENT, result.getPriority());
        assertNotNull(result.getCategory());
        assertNotNull(result.getReasoning());
        assertNotNull(result.getKeywordsFound());
    }

    @Test
    void testHighPriority() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setSubject("Important blocking issue");
        ticket.setDescription("This is blocking our work and we need it fixed asap, need immediately");

        // When
        ClassificationResult result = classificationService.classify(ticket);

        // Then
        assertEquals(Priority.HIGH, result.getPriority());
        assertNotNull(result.getCategory());
        assertNotNull(result.getReasoning());
        assertNotNull(result.getKeywordsFound());
    }

    @Test
    void testLowPriority() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setSubject("Minor cosmetic issue");
        ticket.setDescription("This is a minor suggestion, nice to have change when you get a chance");

        // When
        ClassificationResult result = classificationService.classify(ticket);

        // Then
        assertEquals(Priority.LOW, result.getPriority());
        assertNotNull(result.getCategory());
        assertNotNull(result.getReasoning());
        assertNotNull(result.getKeywordsFound());
    }

    @Test
    void testDefaultPriority() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setSubject("General inquiry");
        ticket.setDescription("I have a question about how to use the dashboard feature properly");

        // When
        ClassificationResult result = classificationService.classify(ticket);

        // Then
        assertEquals(Priority.MEDIUM, result.getPriority());
        assertNotNull(result.getCategory());
        assertNotNull(result.getReasoning());
        assertNotNull(result.getKeywordsFound());
    }
}
