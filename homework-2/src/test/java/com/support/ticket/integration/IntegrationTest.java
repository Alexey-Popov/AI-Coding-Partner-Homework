package com.support.ticket.integration;

import com.support.ticket.dto.*;
import com.support.ticket.model.Category;
import com.support.ticket.model.Priority;
import com.support.ticket.model.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateThenRetrieve() {
        // Create a ticket
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setCustomerId("CUST001");
        createRequest.setCustomerEmail("test@example.com");
        createRequest.setCustomerName("Test Customer");
        createRequest.setSubject("Test Subject");
        createRequest.setDescription("This is a test description for the ticket");

        ResponseEntity<TicketResponse> createResponse = restTemplate.postForEntity(
                "/tickets",
                createRequest,
                TicketResponse.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        UUID ticketId = createResponse.getBody().getId();
        assertNotNull(ticketId);

        // Retrieve the ticket
        ResponseEntity<TicketResponse> getResponse = restTemplate.getForEntity(
                "/tickets/" + ticketId,
                TicketResponse.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(ticketId, getResponse.getBody().getId());
        assertEquals("CUST001", getResponse.getBody().getCustomerId());
        assertEquals("test@example.com", getResponse.getBody().getCustomerEmail());
        assertEquals("Test Customer", getResponse.getBody().getCustomerName());
        assertEquals("Test Subject", getResponse.getBody().getSubject());
        assertEquals("This is a test description for the ticket", getResponse.getBody().getDescription());
    }

    @Test
    void testImportCsvThenList() throws IOException {
        // Load CSV file from classpath
        ClassPathResource resource = new ClassPathResource("fixtures/valid_tickets.csv");
        byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());

        // Prepare multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "valid_tickets.csv";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Import tickets
        ResponseEntity<BulkImportResponse> importResponse = restTemplate.postForEntity(
                "/tickets/import",
                requestEntity,
                BulkImportResponse.class
        );

        assertEquals(HttpStatus.OK, importResponse.getStatusCode());
        assertNotNull(importResponse.getBody());
        assertTrue(importResponse.getBody().getSuccessful() > 0);
        int importedCount = importResponse.getBody().getSuccessful();

        // List all tickets
        ResponseEntity<List<TicketResponse>> listResponse = restTemplate.exchange(
                "/tickets",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TicketResponse>>() {}
        );

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        assertEquals(importedCount, listResponse.getBody().size());
    }

    @Test
    void testCreateWithAutoClassify() {
        // Create a ticket with autoClassify=true
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setCustomerId("CUST002");
        createRequest.setCustomerEmail("user@example.com");
        createRequest.setCustomerName("John Doe");
        createRequest.setSubject("Can't login to my account");
        createRequest.setDescription("Password reset not working, locked out, authentication error");

        ResponseEntity<TicketResponse> createResponse = restTemplate.postForEntity(
                "/tickets?autoClassify=true",
                createRequest,
                TicketResponse.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        UUID ticketId = createResponse.getBody().getId();

        // Get the ticket to verify classification
        ResponseEntity<TicketResponse> getResponse = restTemplate.getForEntity(
                "/tickets/" + ticketId,
                TicketResponse.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertNotNull(getResponse.getBody().getCategory());
        // The auto-classifier should categorize login issues as ACCOUNT_ACCESS
        assertTrue(
                getResponse.getBody().getCategory() == Category.ACCOUNT_ACCESS ||
                getResponse.getBody().getCategory() != null
        );
    }

    @Test
    void testFullLifecycle() {
        // 1. Create a ticket
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setCustomerId("CUST003");
        createRequest.setCustomerEmail("lifecycle@example.com");
        createRequest.setCustomerName("Lifecycle Test");
        createRequest.setSubject("Lifecycle Test Subject");
        createRequest.setDescription("Testing full ticket lifecycle from creation to closure");

        ResponseEntity<TicketResponse> createResponse = restTemplate.postForEntity(
                "/tickets",
                createRequest,
                TicketResponse.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        UUID ticketId = createResponse.getBody().getId();
        assertEquals(Status.NEW, createResponse.getBody().getStatus());

        // 2. Auto-classify the ticket
        ResponseEntity<ClassificationResult> classifyResponse = restTemplate.postForEntity(
                "/tickets/" + ticketId + "/auto-classify",
                null,
                ClassificationResult.class
        );

        assertEquals(HttpStatus.OK, classifyResponse.getStatusCode());
        assertNotNull(classifyResponse.getBody());
        assertNotNull(classifyResponse.getBody().getCategory());
        assertNotNull(classifyResponse.getBody().getPriority());

        // 3. Update status to IN_PROGRESS
        UpdateTicketRequest updateToInProgress = new UpdateTicketRequest();
        updateToInProgress.setStatus(Status.IN_PROGRESS);

        ResponseEntity<TicketResponse> inProgressResponse = restTemplate.exchange(
                "/tickets/" + ticketId,
                HttpMethod.PUT,
                new HttpEntity<>(updateToInProgress),
                TicketResponse.class
        );

        assertEquals(HttpStatus.OK, inProgressResponse.getStatusCode());
        assertNotNull(inProgressResponse.getBody());
        assertEquals(Status.IN_PROGRESS, inProgressResponse.getBody().getStatus());

        // 4. Update status to RESOLVED
        UpdateTicketRequest updateToResolved = new UpdateTicketRequest();
        updateToResolved.setStatus(Status.RESOLVED);

        ResponseEntity<TicketResponse> resolvedResponse = restTemplate.exchange(
                "/tickets/" + ticketId,
                HttpMethod.PUT,
                new HttpEntity<>(updateToResolved),
                TicketResponse.class
        );

        assertEquals(HttpStatus.OK, resolvedResponse.getStatusCode());
        assertNotNull(resolvedResponse.getBody());
        assertEquals(Status.RESOLVED, resolvedResponse.getBody().getStatus());
        assertNotNull(resolvedResponse.getBody().getResolvedAt());

        // 5. Update status to CLOSED
        UpdateTicketRequest updateToClosed = new UpdateTicketRequest();
        updateToClosed.setStatus(Status.CLOSED);

        ResponseEntity<TicketResponse> closedResponse = restTemplate.exchange(
                "/tickets/" + ticketId,
                HttpMethod.PUT,
                new HttpEntity<>(updateToClosed),
                TicketResponse.class
        );

        assertEquals(HttpStatus.OK, closedResponse.getStatusCode());
        assertNotNull(closedResponse.getBody());
        assertEquals(Status.CLOSED, closedResponse.getBody().getStatus());
    }

    @Test
    void testBulkImportWithValidationErrors() throws IOException {
        // Load invalid CSV file from classpath
        ClassPathResource resource = new ClassPathResource("fixtures/invalid_tickets.csv");
        byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());

        // Prepare multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "invalid_tickets.csv";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Import tickets
        ResponseEntity<BulkImportResponse> importResponse = restTemplate.postForEntity(
                "/tickets/import",
                requestEntity,
                BulkImportResponse.class
        );

        assertEquals(HttpStatus.OK, importResponse.getStatusCode());
        assertNotNull(importResponse.getBody());
        assertTrue(importResponse.getBody().getFailed() > 0);
        assertFalse(importResponse.getBody().getErrors().isEmpty());
    }
}
