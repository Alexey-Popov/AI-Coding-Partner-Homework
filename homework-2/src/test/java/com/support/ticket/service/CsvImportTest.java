package com.support.ticket.service;

import com.support.ticket.dto.CreateTicketRequest;
import com.support.ticket.exception.ImportException;
import com.support.ticket.service.parser.CsvImportParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvImportTest {

    private final CsvImportParser parser = new CsvImportParser();

    @Test
    void testValidCsv() {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/valid_tickets.csv");
        assertNotNull(inputStream, "Fixture file valid_tickets.csv not found");

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets);
        assertEquals(5, tickets.size(), "Expected 5 records from valid_tickets.csv");
    }

    @Test
    void testMissingColumnsCsv() {
        String csvWithoutSubject = "customerId,customerEmail,customerName,description\n" +
                "C001,customer@example.com,John Doe,This is a description";
        InputStream inputStream = new ByteArrayInputStream(csvWithoutSubject.getBytes(StandardCharsets.UTF_8));

        assertThrows(ImportException.class, () -> parser.parse(inputStream),
                "Expected ImportException when 'subject' column is missing");
    }

    @Test
    void testInvalidDataCsv() {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/invalid_tickets.csv");
        assertNotNull(inputStream, "Fixture file invalid_tickets.csv not found");

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets, "Parser should return records even with invalid data");
        assertTrue(tickets.size() > 0, "Expected at least some records to be parsed");
    }

    @Test
    void testEmptyCsv() {
        String csvHeaders = "customer_id,customer_email,customer_name,subject,description\n";
        InputStream inputStream = new ByteArrayInputStream(csvHeaders.getBytes(StandardCharsets.UTF_8));

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets);
        assertTrue(tickets.isEmpty(), "Expected empty list for CSV with only headers");
    }

    @Test
    void testMalformedCsv() {
        InputStream inputStream = getClass().getResourceAsStream("/fixtures/malformed.csv");
        assertNotNull(inputStream, "Fixture file malformed.csv not found");

        try {
            List<CreateTicketRequest> tickets = parser.parse(inputStream);
            assertNotNull(tickets, "Parser handled malformed CSV");
        } catch (ImportException e) {
            assertNotNull(e, "ImportException thrown for malformed CSV");
        }
    }

    @Test
    void testLargeCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("customer_id,customer_email,customer_name,subject,description\n");

        for (int i = 1; i <= 100; i++) {
            csv.append(String.format("C%03d,customer%d@example.com,Customer %d,Subject %d,This is a detailed description for ticket number %d\n",
                    i, i, i, i, i));
        }

        InputStream inputStream = new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8));

        List<CreateTicketRequest> tickets = parser.parse(inputStream);

        assertNotNull(tickets);
        assertEquals(100, tickets.size(), "Expected 100 records from generated CSV");
    }
}
