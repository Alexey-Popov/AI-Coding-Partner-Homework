package com.support.service;

import com.support.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportCsvTest {
    
    private ImportService importService;
    private TicketService ticketService;
    
    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        importService = new ImportService(ticketService);
    }
    
    @Test
    void testImportValidCsv() throws IOException {
        String csvContent = """
            customer_id,customer_email,customer_name,subject,description,category,priority,status
            CUST-001,john@example.com,John Doe,Login Issue,Cannot login to account,ACCOUNT_ACCESS,HIGH,NEW
            CUST-002,jane@example.com,Jane Smith,Bug Report,Application crashes on startup,BUG_REPORT,URGENT,NEW
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        
        when(ticketService.createTicket(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
        
        var result = importService.importFile(file, "csv");
        
        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getSuccessfulRecords());
        assertEquals(0, result.getFailedRecords());
    }
    
    @Test
    void testImportCsvWithInvalidData() throws IOException {
        String csvContent = """
            customer_id,customer_email,customer_name,subject,description,category,priority,status
            CUST-001,invalid-email,John Doe,Test,Short,OTHER,MEDIUM,NEW
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        
        when(ticketService.createTicket(any(Ticket.class)))
            .thenThrow(new IllegalArgumentException("Validation error"));
        
        var result = importService.importFile(file, "csv");
        
        assertEquals(1, result.getTotalRecords());
        assertEquals(0, result.getSuccessfulRecords());
        assertEquals(1, result.getFailedRecords());
        assertFalse(result.getErrors().isEmpty());
    }
    
    @Test
    void testImportEmptyCsv() throws IOException {
        String csvContent = "customer_id,customer_email,customer_name,subject,description\n";
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        
        var result = importService.importFile(file, "csv");
        
        assertEquals(0, result.getTotalRecords());
    }
    
    @Test
    void testImportCsvWithMissingColumns() throws IOException {
        String csvContent = """
            customer_id,customer_email
            CUST-001,john@example.com
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        
        when(ticketService.createTicket(any(Ticket.class)))
            .thenThrow(new IllegalArgumentException("Missing required fields"));
        
        var result = importService.importFile(file, "csv");
        
        assertEquals(1, result.getFailedRecords());
    }
    
    @Test
    void testImportMalformedCsv() {
        String csvContent = "not,a,valid\ncsv\"file";
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        
        assertThrows(IOException.class, () -> importService.importFile(file, "csv"));
    }
    
    @Test
    void testImportCsvPartialSuccess() throws IOException {
        String csvContent = """
            customer_id,customer_email,customer_name,subject,description,category,priority,status
            CUST-001,john@example.com,John Doe,Valid Ticket,This is a valid description,OTHER,MEDIUM,NEW
            CUST-002,invalid,Bad Data,Bad,x,OTHER,MEDIUM,NEW
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));
        
        when(ticketService.createTicket(any(Ticket.class)))
            .thenReturn(null)
            .thenThrow(new IllegalArgumentException("Validation failed"));
        
        var result = importService.importFile(file, "csv");
        
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getSuccessfulRecords());
        assertEquals(1, result.getFailedRecords());
    }
}
