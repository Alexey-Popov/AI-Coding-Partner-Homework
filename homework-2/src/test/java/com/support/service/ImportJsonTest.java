package com.support.service;

import com.support.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportJsonTest {
    
    private ImportService importService;
    private TicketService ticketService;
    
    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        importService = new ImportService(ticketService);
    }
    
    @Test
    void testImportValidJsonArray() throws IOException {
        String jsonContent = """
            [
                {
                    "customer_id": "CUST-001",
                    "customer_email": "john@example.com",
                    "customer_name": "John Doe",
                    "subject": "Login Issue",
                    "description": "Cannot login to my account",
                    "category": "ACCOUNT_ACCESS",
                    "priority": "HIGH"
                }
            ]
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.json", "application/json", 
            jsonContent.getBytes(StandardCharsets.UTF_8));
        
        when(ticketService.createTicket(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
        
        var result = importService.importFile(file, "json");
        
        assertEquals(1, result.getTotalRecords());
        assertEquals(1, result.getSuccessfulRecords());
    }
    
    @Test
    void testImportValidJsonObject() throws IOException {
        String jsonContent = """
            {
                "customer_id": "CUST-001",
                "customer_email": "john@example.com",
                "customer_name": "John Doe",
                "subject": "Test Issue",
                "description": "This is a test description"
            }
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "ticket.json", "application/json", 
            jsonContent.getBytes(StandardCharsets.UTF_8));
        
        when(ticketService.createTicket(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
        
        var result = importService.importFile(file, "json");
        
        assertEquals(1, result.getTotalRecords());
    }
    
    @Test
    void testImportInvalidJson() {
        String jsonContent = "{invalid json}";
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.json", "application/json", 
            jsonContent.getBytes(StandardCharsets.UTF_8));
        
        assertThrows(IOException.class, () -> importService.importFile(file, "json"));
    }
    
    @Test
    void testImportJsonWithMultipleRecords() throws IOException {
        String jsonContent = """
            [
                {
                    "customer_id": "CUST-001",
                    "customer_email": "john@example.com",
                    "customer_name": "John Doe",
                    "subject": "Issue 1",
                    "description": "Description 1"
                },
                {
                    "customer_id": "CUST-002",
                    "customer_email": "jane@example.com",
                    "customer_name": "Jane Smith",
                    "subject": "Issue 2",
                    "description": "Description 2"
                }
            ]
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.json", "application/json", 
            jsonContent.getBytes(StandardCharsets.UTF_8));
        
        when(ticketService.createTicket(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
        
        var result = importService.importFile(file, "json");
        
        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getSuccessfulRecords());
    }
    
    @Test
    void testImportEmptyJsonArray() throws IOException {
        String jsonContent = "[]";
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.json", "application/json", 
            jsonContent.getBytes(StandardCharsets.UTF_8));
        
        var result = importService.importFile(file, "json");
        
        assertEquals(0, result.getTotalRecords());
    }
}
