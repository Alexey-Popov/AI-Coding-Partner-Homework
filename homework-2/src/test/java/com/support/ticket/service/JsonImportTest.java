package com.support.ticket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.ticket.dto.CreateTicketRequest;
import com.support.ticket.exception.ImportException;
import com.support.ticket.service.parser.JsonImportParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonImportTest {

    private JsonImportParser parser;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        parser = new JsonImportParser(objectMapper);
    }

    @Test
    void testValidJsonArray() {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/valid_tickets.json");
        assertNotNull(inputStream, "Fixture file valid_tickets.json not found");

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets);
        assertEquals(3, tickets.size(), "Expected 3 records from valid_tickets.json");
    }

    @Test
    void testInvalidJsonSyntax() {
        String brokenJson = "{ broken json";
        InputStream inputStream = new ByteArrayInputStream(brokenJson.getBytes(StandardCharsets.UTF_8));

        assertThrows(ImportException.class, () -> parser.parse(inputStream),
                "Expected ImportException for invalid JSON syntax");
    }

    @Test
    void testJsonWithMissingFields() {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/invalid_tickets.json");
        assertNotNull(inputStream, "Fixture file invalid_tickets.json not found");

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets, "Parser should return records even with missing fields");
        assertEquals(2, tickets.size(), "Expected 2 records from invalid_tickets.json");
    }

    @Test
    void testEmptyJsonArray() {
        String emptyJson = "[]";
        InputStream inputStream = new ByteArrayInputStream(emptyJson.getBytes(StandardCharsets.UTF_8));

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets);
        assertTrue(tickets.isEmpty(), "Expected empty list for empty JSON array");
    }

    @Test
    void testSingleObject() {
        String singleObjectJson = "{\"customerId\":\"C1\",\"customerEmail\":\"a@b.com\"," +
                "\"customerName\":\"Test\",\"subject\":\"Test\"," +
                "\"description\":\"A long enough description\"}";
        InputStream inputStream = new ByteArrayInputStream(singleObjectJson.getBytes(StandardCharsets.UTF_8));

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets);
        assertEquals(1, tickets.size(), "Expected list with 1 record for single JSON object");

        CreateTicketRequest ticket = tickets.get(0);
        assertEquals("C1", ticket.getCustomerId());
        assertEquals("a@b.com", ticket.getCustomerEmail());
        assertEquals("Test", ticket.getCustomerName());
        assertEquals("Test", ticket.getSubject());
        assertEquals("A long enough description", ticket.getDescription());
    }
}
