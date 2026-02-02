package com.support.service;

import com.support.dto.ClassificationResult;
import com.support.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategorizationTest {
    
    private ClassificationService classificationService;
    
    @BeforeEach
    void setUp() {
        classificationService = new ClassificationService();
    }
    
    @Test
    void testClassifyAccountAccess() {
        ClassificationResult result = classificationService.classifyTicket(
            "Cannot login", "I forgot my password and can't access my account");
        
        assertEquals(Ticket.Category.ACCOUNT_ACCESS, result.getCategory());
        assertTrue(result.getConfidence() > 0.5);
    }
    
    @Test
    void testClassifyTechnicalIssue() {
        ClassificationResult result = classificationService.classifyTicket(
            "Application Error", "The app crashes when I click the submit button");
        
        assertEquals(Ticket.Category.TECHNICAL_ISSUE, result.getCategory());
    }
    
    @Test
    void testClassifyBillingQuestion() {
        ClassificationResult result = classificationService.classifyTicket(
            "Refund Request", "I was charged twice for my subscription");
        
        assertEquals(Ticket.Category.BILLING_QUESTION, result.getCategory());
    }
    
    @Test
    void testClassifyFeatureRequest() {
        ClassificationResult result = classificationService.classifyTicket(
            "Enhancement", "Could you add dark mode to the app?");
        
        assertEquals(Ticket.Category.FEATURE_REQUEST, result.getCategory());
    }
    
    @Test
    void testClassifyBugReport() {
        ClassificationResult result = classificationService.classifyTicket(
            "Bug Found", "Steps to reproduce: 1. Click button 2. App crashes");
        
        assertEquals(Ticket.Category.BUG_REPORT, result.getCategory());
    }
    
    @Test
    void testClassifyUrgentPriority() {
        ClassificationResult result = classificationService.classifyTicket(
            "Critical Issue", "Production is down, can't access the system");
        
        assertEquals(Ticket.Priority.URGENT, result.getPriority());
    }
    
    @Test
    void testClassifyHighPriority() {
        ClassificationResult result = classificationService.classifyTicket(
            "Important", "This is blocking our work, need it ASAP");
        
        assertEquals(Ticket.Priority.HIGH, result.getPriority());
    }
    
    @Test
    void testClassifyLowPriority() {
        ClassificationResult result = classificationService.classifyTicket(
            "Minor Issue", "Just a cosmetic suggestion for the UI");
        
        assertEquals(Ticket.Priority.LOW, result.getPriority());
    }
    
    @Test
    void testClassifyMediumPriorityDefault() {
        ClassificationResult result = classificationService.classifyTicket(
            "General Question", "How do I use this feature?");
        
        assertEquals(Ticket.Priority.MEDIUM, result.getPriority());
    }
    
    @Test
    void testClassificationConfidence() {
        ClassificationResult result = classificationService.classifyTicket(
            "Login password 2FA", "Critical security issue with authentication");
        
        assertTrue(result.getConfidence() > 0.8);
        assertFalse(result.getKeywordsFound().isEmpty());
    }
}
