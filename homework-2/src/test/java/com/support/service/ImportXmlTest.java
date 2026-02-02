package com.support.service;

import com.support.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportXmlTest {
    
    private ImportService importService;
    private TicketService ticketService;
    
    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        importService = new ImportService(ticketService);
    }
    
    @Test
    void testImportValidXml() throws IOException {
        String xmlContent = """
            <tickets>
                <ticket>
                    <customer_id>CUST-001</customer_id>
                    <customer_email>john@example.com</customer_email>
                    <customer_name>John Doe</customer_name>
                    <subject>Login Issue</subject>
                    <description>Cannot login to account</description>
                    <category>ACCOUNT_ACCESS</category>
                    <priority>HIGH</priority>
                </ticket>
            </tickets>
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.xml", "application/xml", 
            xmlContent.getBytes(StandardCharsets.UTF_8));
        
        when(ticketService.createTicket(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
        
        var result = importService.importFile(file, "xml");
        
        assertTrue(result.getTotalRecords() >= 1);
    }
    
    @Test
    void testImportXmlSingleTicket() throws IOException {
        String xmlContent = """
            <ticket>
                <customer_id>CUST-001</customer_id>
                <customer_email>john@example.com</customer_email>
                <customer_name>John Doe</customer_name>
                <subject>Test Issue</subject>
                <description>This is a test description</description>
            </ticket>
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "ticket.xml", "application/xml", 
            xmlContent.getBytes(StandardCharsets.UTF_8));
        
        when(ticketService.createTicket(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
        
        var result = importService.importFile(file, "xml");
        
        assertTrue(result.getTotalRecords() >= 1);
    }
    
    @Test
    void testImportInvalidXml() {
        String xmlContent = "<invalid><xml>";
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.xml", "application/xml", 
            xmlContent.getBytes(StandardCharsets.UTF_8));
        
        assertThrows(IOException.class, () -> importService.importFile(file, "xml"));
    }
    
    @Test
    void testImportXmlWithMultipleTickets() throws IOException {
        String xmlContent = """
            <tickets>
                <ticket>
                    <customer_id>CUST-001</customer_id>
                    <customer_email>john@example.com</customer_email>
                    <customer_name>John Doe</customer_name>
                    <subject>Issue 1</subject>
                    <description>Description 1</description>
                </ticket>
                <ticket>
                    <customer_id>CUST-002</customer_id>
                    <customer_email>jane@example.com</customer_email>
                    <customer_name>Jane Smith</customer_name>
                    <subject>Issue 2</subject>
                    <description>Description 2</description>
                </ticket>
            </tickets>
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.xml", "application/xml", 
            xmlContent.getBytes(StandardCharsets.UTF_8));
        
        when(ticketService.createTicket(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
        
        var result = importService.importFile(file, "xml");
        
        assertTrue(result.getTotalRecords() >= 1);
    }
    
    @Test
    void testImportUnsupportedFormat() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.txt", "text/plain", "content".getBytes());
        
        assertThrows(IOException.class, () -> importService.importFile(file, "txt"));
    }
}
