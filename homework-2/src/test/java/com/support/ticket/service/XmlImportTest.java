package com.support.ticket.service;

import com.support.ticket.dto.CreateTicketRequest;
import com.support.ticket.exception.ImportException;
import com.support.ticket.service.parser.XmlImportParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XmlImportTest {

    private final XmlImportParser parser = new XmlImportParser();

    @Test
    void testValidXml() {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/valid_tickets.xml");
        assertNotNull(inputStream, "Fixture file valid_tickets.xml not found");

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets);
        assertEquals(2, tickets.size(), "Expected 2 records from valid_tickets.xml");
    }

    @Test
    void testMalformedXml() {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/malformed.xml");
        assertNotNull(inputStream, "Fixture file malformed.xml not found");

        assertThrows(ImportException.class, () -> parser.parse(inputStream),
                "Expected ImportException for malformed XML");
    }

    @Test
    void testMissingElements() {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/invalid_tickets.xml");
        assertNotNull(inputStream, "Fixture file invalid_tickets.xml not found");

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets, "Parser should return records even with missing elements");
        assertEquals(2, tickets.size(), "Expected 2 records from invalid_tickets.xml");
    }

    @Test
    void testEmptyXml() {
        String emptyXml = "";
        InputStream inputStream = new ByteArrayInputStream(emptyXml.getBytes(StandardCharsets.UTF_8));

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets);
        assertTrue(tickets.isEmpty(), "Expected empty list for empty XML");
    }

    @Test
    void testInvalidXmlContent() {
        // XML with unclosed tags and invalid structure
        String invalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<tickets><ticket><customerId>C001<</ticket></tickets>";
        InputStream inputStream = new ByteArrayInputStream(invalidXml.getBytes(StandardCharsets.UTF_8));

        assertThrows(ImportException.class, () -> parser.parse(inputStream),
                "Expected ImportException for invalid XML content");
    }
}
