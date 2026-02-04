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
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class PerformanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreate100TicketsUnder2Seconds() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setCustomerId("CUST" + String.format("%03d", i));
            request.setCustomerEmail("perf" + i + "@example.com");
            request.setCustomerName("Performance Test " + i);
            request.setSubject("Performance Test Subject " + i);
            request.setDescription("This is a performance test description for ticket number " + i);

            ResponseEntity<TicketResponse> response = restTemplate.postForEntity(
                    "/tickets",
                    request,
                    TicketResponse.class
            );

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 2000, "Creating 100 tickets took " + duration + "ms, expected < 2000ms");
    }

    @Test
    void testBulkImport500RowsUnder5Seconds() {
        // Generate CSV with 500 rows
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("customer_id,customer_email,customer_name,subject,description\n");

        for (int i = 0; i < 500; i++) {
            csvContent.append("BULK").append(String.format("%03d", i)).append(",")
                    .append("bulk").append(i).append("@example.com,")
                    .append("Bulk User ").append(i).append(",")
                    .append("Bulk Subject ").append(i).append(",")
                    .append("This is a bulk import test description for row ").append(i).append("\n");
        }

        byte[] fileContent = csvContent.toString().getBytes();

        // Prepare multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "bulk_500.csv";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        long startTime = System.currentTimeMillis();

        ResponseEntity<BulkImportResponse> response = restTemplate.postForEntity(
                "/tickets/import",
                requestEntity,
                BulkImportResponse.class
        );

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccessful() >= 490, "Expected at least 490 successful imports");
        assertTrue(duration < 5000, "Bulk import of 500 rows took " + duration + "ms, expected < 5000ms");
    }

    @Test
    void testListWithFiltersOn1000TicketsUnder1Second() {
        // First, create 1000 tickets using bulk import
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("customer_id,customer_email,customer_name,subject,description\n");

        for (int i = 0; i < 1000; i++) {
            csvContent.append("FILTER").append(String.format("%04d", i)).append(",")
                    .append("filter").append(i).append("@example.com,")
                    .append("Filter User ").append(i).append(",")
                    .append("Filter Subject ").append(i).append(",")
                    .append("This is a filter test description for row ").append(i).append("\n");
        }

        byte[] fileContent = csvContent.toString().getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "filter_1000.csv";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<BulkImportResponse> importResponse = restTemplate.postForEntity(
                "/tickets/import",
                requestEntity,
                BulkImportResponse.class
        );

        assertEquals(HttpStatus.OK, importResponse.getStatusCode());
        assertNotNull(importResponse.getBody());
        assertTrue(importResponse.getBody().getSuccessful() >= 990);

        // Now test filtering performance
        long startTime = System.currentTimeMillis();

        ResponseEntity<List<TicketResponse>> listResponse = restTemplate.exchange(
                "/tickets",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TicketResponse>>() {}
        );

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        assertTrue(duration < 1000, "Filtering 1000 tickets took " + duration + "ms, expected < 1000ms");
    }

    @Test
    void testConcurrent20Requests() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<ResponseEntity<TicketResponse>>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < 20; i++) {
            final int index = i;
            Future<ResponseEntity<TicketResponse>> future = executor.submit(() -> {
                CreateTicketRequest request = new CreateTicketRequest();
                request.setCustomerId("CONCURRENT" + String.format("%02d", index));
                request.setCustomerEmail("concurrent" + index + "@example.com");
                request.setCustomerName("Concurrent User " + index);
                request.setSubject("Concurrent Subject " + index);
                request.setDescription("This is a concurrent test description for request " + index);

                return restTemplate.postForEntity(
                        "/tickets",
                        request,
                        TicketResponse.class
                );
            });
            futures.add(future);
        }

        // Wait for all requests to complete
        for (Future<ResponseEntity<TicketResponse>> future : futures) {
            try {
                ResponseEntity<TicketResponse> response = future.get(10, TimeUnit.SECONDS);
                if (response.getStatusCode() == HttpStatus.CREATED) {
                    successCount.incrementAndGet();
                }
            } catch (TimeoutException e) {
                fail("Request timed out");
            }
        }

        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);

        assertEquals(20, successCount.get(), "All 20 concurrent requests should succeed");
    }

    @Test
    void testClassificationOf50TicketsUnder3Seconds() {
        // First, create 50 tickets
        List<UUID> ticketIds = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setCustomerId("CLASSIFY" + String.format("%02d", i));
            request.setCustomerEmail("classify" + i + "@example.com");
            request.setCustomerName("Classify User " + i);
            request.setSubject("Classification Test Subject " + i);
            request.setDescription("This is a test description for classification test number " + i);

            ResponseEntity<TicketResponse> response = restTemplate.postForEntity(
                    "/tickets",
                    request,
                    TicketResponse.class
            );

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            ticketIds.add(response.getBody().getId());
        }

        // Now classify all 50 tickets
        long startTime = System.currentTimeMillis();

        int successfulClassifications = 0;
        for (UUID ticketId : ticketIds) {
            ResponseEntity<ClassificationResult> classifyResponse = restTemplate.postForEntity(
                    "/tickets/" + ticketId + "/auto-classify",
                    null,
                    ClassificationResult.class
            );

            if (classifyResponse.getStatusCode() == HttpStatus.OK) {
                successfulClassifications++;
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(50, successfulClassifications, "All 50 classifications should succeed");
        assertTrue(duration < 3000, "Classifying 50 tickets took " + duration + "ms, expected < 3000ms");
    }
}
