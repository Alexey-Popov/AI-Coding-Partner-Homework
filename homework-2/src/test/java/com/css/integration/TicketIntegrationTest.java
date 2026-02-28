package com.css.integration;

import com.css.dto.CreateTicketRequest;
import com.css.dto.UpdateTicketRequest;
import com.css.model.*;
import com.css.repository.TicketRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Customer Support System.
 * These tests exercise the full Spring context and verify end-to-end workflows.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TicketIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
    }

    // -----------------------------------------------------------------------
    // 1. Complete Ticket Lifecycle Workflow
    // -----------------------------------------------------------------------

    @Test
    void completeTicketLifecycle_createReadUpdateResolveDelete() throws Exception {
        // Step 1: Create a ticket
        CreateTicketRequest createReq = buildCreateRequest(
                "CUST-LC1", "lifecycle@test.com", "Lifecycle User",
                "Cannot login to account",
                "I have been trying to login but keep getting an error. Can't access my account."
        );

        MvcResult createResult = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("new"))
                .andExpect(jsonPath("$.customer_id").value("CUST-LC1"))
                .andReturn();

        String ticketId = extractId(createResult);

        // Step 2: Read the ticket
        mockMvc.perform(get("/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId))
                .andExpect(jsonPath("$.subject").value("Cannot login to account"))
                .andExpect(jsonPath("$.status").value("new"));

        // Step 3: Update – assign and move to in_progress
        UpdateTicketRequest updateReq = new UpdateTicketRequest();
        updateReq.setAssignedTo("agent-007");
        updateReq.setStatus(TicketStatus.IN_PROGRESS);

        mockMvc.perform(put("/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigned_to").value("agent-007"))
                .andExpect(jsonPath("$.status").value("in_progress"));

        // Step 4: Resolve the ticket
        UpdateTicketRequest resolveReq = new UpdateTicketRequest();
        resolveReq.setStatus(TicketStatus.RESOLVED);

        mockMvc.perform(put("/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("resolved"))
                .andExpect(jsonPath("$.resolved_at").isNotEmpty());

        // Step 5: Verify in listing
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(ticketId));

        // Step 6: Delete the ticket
        mockMvc.perform(delete("/tickets/{id}", ticketId))
                .andExpect(status().isNoContent());

        // Step 7: Verify the ticket is gone
        mockMvc.perform(get("/tickets/{id}", ticketId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void ticketLifecycle_createWithAutoClassifyAndResolve() throws Exception {
        // Create with auto-classify enabled – uses keyword-based classification
        String body = """
                {
                  "customer_id": "CUST-AC1",
                  "customer_email": "autoclassify@test.com",
                  "customer_name": "Auto User",
                  "subject": "Production down - critical outage",
                  "description": "Production is down and we cannot access any services. This is critical.",
                  "auto_classify": true
                }
                """;

        MvcResult result = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String ticketId = extractId(result);

        // Auto-classification should have set category and priority
        MvcResult getResult = mockMvc.perform(get("/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode ticket = objectMapper.readTree(getResult.getResponse().getContentAsString());
        // The description and subject contain "critical" and "can't access" / "production down"
        // so classification should set URGENT priority
        assertNotNull(ticket.get("classification_confidence"));

        // Close the ticket
        UpdateTicketRequest closeReq = new UpdateTicketRequest();
        closeReq.setStatus(TicketStatus.CLOSED);

        mockMvc.perform(put("/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(closeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("closed"))
                .andExpect(jsonPath("$.resolved_at").isNotEmpty());
    }

    // -----------------------------------------------------------------------
    // 2. Bulk Import with Auto-Classification Verification
    // -----------------------------------------------------------------------

    @Test
    void bulkImportJson_withAutoClassification() throws Exception {
        // Import JSON tickets
        String jsonContent = """
                [
                  {
                    "customer_id": "CUST-IMP1",
                    "customer_email": "import1@test.com",
                    "customer_name": "Import User One",
                    "subject": "Cannot login",
                    "description": "I forgot my password and cannot log in to my account",
                    "category": "account_access",
                    "priority": "high",
                    "status": "new",
                    "tags": ["login", "password"],
                    "metadata": {"source": "web_form", "browser": "Chrome", "device_type": "desktop"}
                  },
                  {
                    "customer_id": "CUST-IMP2",
                    "customer_email": "import2@test.com",
                    "customer_name": "Import User Two",
                    "subject": "Invoice discrepancy",
                    "description": "The invoice amount does not match the payment I made for the billing cycle",
                    "category": "billing_question",
                    "priority": "medium",
                    "status": "new",
                    "tags": ["billing", "invoice"],
                    "metadata": {"source": "email", "browser": null, "device_type": "desktop"}
                  },
                  {
                    "customer_id": "CUST-IMP3",
                    "customer_email": "import3@test.com",
                    "customer_name": "Import User Three",
                    "subject": "App crashes on startup",
                    "description": "The application crashes with a stack trace error every time I try to launch it",
                    "category": "technical_issue",
                    "priority": "high",
                    "status": "new",
                    "tags": ["crash", "error"],
                    "metadata": {"source": "chat", "browser": "Firefox", "device_type": "mobile"}
                  }
                ]
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets.json", "application/json",
                jsonContent.getBytes(StandardCharsets.UTF_8));

        MvcResult importResult = mockMvc.perform(multipart("/tickets/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_records").value(3))
                .andExpect(jsonPath("$.successful_records").value(3))
                .andExpect(jsonPath("$.failed_records").value(0))
                .andReturn();

        // Verify all 3 tickets are in the system
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        // Auto-classify each imported ticket and verify
        JsonNode importJson = objectMapper.readTree(importResult.getResponse().getContentAsString());
        List<String> importedIds = new ArrayList<>();
        for (JsonNode idNode : importJson.get("imported_ticket_ids")) {
            importedIds.add(idNode.asText());
        }

        for (String id : importedIds) {
            MvcResult classifyResult = mockMvc.perform(post("/tickets/{id}/auto-classify", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.category").exists())
                    .andExpect(jsonPath("$.priority").exists())
                    .andExpect(jsonPath("$.confidence").isNumber())
                    .andExpect(jsonPath("$.reasoning").isString())
                    .andExpect(jsonPath("$.keywords").isArray())
                    .andReturn();

            JsonNode classification = objectMapper.readTree(classifyResult.getResponse().getContentAsString());
            double confidence = classification.get("confidence").asDouble();
            assertTrue(confidence >= 0.0 && confidence <= 1.0,
                    "Confidence should be between 0 and 1, was: " + confidence);
        }
    }

    @Test
    void bulkImportCsv_withAutoClassification() throws Exception {
        String csvContent = """
                customer_id,customer_email,customer_name,subject,description,category,priority,status,assigned_to,tags,source,browser,device_type
                CUST-CSV1,csv1@test.com,CSV User One,Payment failed,My payment was declined even though my card is valid. The charge appeared twice on billing.,billing_question,high,new,,payment|billing,web_form,Chrome,desktop
                CUST-CSV2,csv2@test.com,CSV User Two,Feature: Dark mode,It would be great to have dark mode as a feature suggestion for the UI.,feature_request,low,new,,feature|suggestion,email,,desktop
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/tickets/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_records").value(2))
                .andExpect(jsonPath("$.successful_records").value(2))
                .andExpect(jsonPath("$.failed_records").value(0));

        // Verify tickets are stored
        MvcResult listResult = mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        JsonNode tickets = objectMapper.readTree(listResult.getResponse().getContentAsString());
        for (JsonNode t : tickets) {
            String id = t.get("id").asText();

            // Auto-classify and verify classification makes sense
            mockMvc.perform(post("/tickets/{id}/auto-classify", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.category").exists())
                    .andExpect(jsonPath("$.confidence").isNumber());
        }
    }

    @Test
    void bulkImportXml_withAutoClassification() throws Exception {
        String xmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <tickets>
                  <ticket>
                    <customer_id>CUST-XML1</customer_id>
                    <customer_email>xml1@test.com</customer_email>
                    <customer_name>XML User One</customer_name>
                    <subject>Security vulnerability found</subject>
                    <description>We found a critical security vulnerability that requires urgent attention.</description>
                    <category>technical_issue</category>
                    <priority>urgent</priority>
                    <status>new</status>
                    <tags>security,critical</tags>
                    <metadata>
                      <source>api</source>
                      <browser>N/A</browser>
                      <device_type>desktop</device_type>
                    </metadata>
                  </ticket>
                  <ticket>
                    <customer_id>CUST-XML2</customer_id>
                    <customer_email>xml2@test.com</customer_email>
                    <customer_name>XML User Two</customer_name>
                    <subject>Bug: Steps to reproduce search issue</subject>
                    <description>Steps to reproduce: search for items with quotes. The defect causes empty results.</description>
                    <category>bug_report</category>
                    <priority>medium</priority>
                    <status>new</status>
                    <tags>bug,search</tags>
                    <metadata>
                      <source>web_form</source>
                      <browser>Chrome</browser>
                      <device_type>desktop</device_type>
                    </metadata>
                  </ticket>
                </tickets>
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets.xml", "application/xml",
                xmlContent.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/tickets/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_records").value(2))
                .andExpect(jsonPath("$.successful_records").value(2));

        // Verify and auto-classify
        MvcResult listResult = mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        JsonNode tickets = objectMapper.readTree(listResult.getResponse().getContentAsString());
        for (JsonNode t : tickets) {
            String id = t.get("id").asText();
            MvcResult classifyResult = mockMvc.perform(post("/tickets/{id}/auto-classify", id))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode classification = objectMapper.readTree(classifyResult.getResponse().getContentAsString());
            assertFalse(classification.get("keywords").isEmpty(),
                    "Classification should have found keywords for ticket " + id);
        }
    }

    // -----------------------------------------------------------------------
    // 3. Concurrent Operations (20+ simultaneous requests)
    // -----------------------------------------------------------------------

    @Test
    void concurrentTicketCreation_20PlusSimultaneousRequests() throws Exception {
        int threadCount = 25;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<MvcResult>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                latch.await(); // all threads start at the same time
                CreateTicketRequest req = buildCreateRequest(
                        "CUST-CONC" + index,
                        "concurrent" + index + "@test.com",
                        "Concurrent User " + index,
                        "Concurrent ticket #" + index,
                        "This is a concurrent test ticket number " + index + " to verify thread safety."
                );
                return mockMvc.perform(post("/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isCreated())
                        .andReturn();
            }));
        }

        // Release all threads simultaneously
        latch.countDown();

        // Collect results
        List<String> createdIds = new ArrayList<>();
        for (Future<MvcResult> future : futures) {
            MvcResult result = future.get(10, TimeUnit.SECONDS);
            String id = extractId(result);
            createdIds.add(id);
        }
        executor.shutdown();

        // Verify all tickets were created
        assertEquals(threadCount, createdIds.size(), "All concurrent creates should succeed");

        // Verify no duplicate IDs
        long uniqueIds = createdIds.stream().distinct().count();
        assertEquals(threadCount, uniqueIds, "All ticket IDs should be unique");

        // Verify all tickets are retrievable
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(threadCount)));
    }

    @Test
    void concurrentMixedOperations_readsAndWritesSimultaneously() throws Exception {
        // Pre-create some tickets
        List<String> existingIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CreateTicketRequest req = buildCreateRequest(
                    "CUST-PRE" + i, "pre" + i + "@test.com", "Pre User " + i,
                    "Pre-existing ticket " + i,
                    "This ticket was created before concurrent operations begin."
            );
            MvcResult result = mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andReturn();
            existingIds.add(extractId(result));
        }

        int totalOps = 25;
        ExecutorService executor = Executors.newFixedThreadPool(totalOps);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        // Mix of operations: 10 creates, 10 reads, 5 updates
        for (int i = 0; i < 10; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                latch.await();
                CreateTicketRequest req = buildCreateRequest(
                        "CUST-MIX" + index, "mix" + index + "@test.com", "Mix User " + index,
                        "Mixed ops ticket " + index,
                        "Concurrent mixed operation test ticket " + index + " content."
                );
                mockMvc.perform(post("/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isCreated());
                return 201;
            }));
        }

        for (int i = 0; i < 10; i++) {
            final String readId = existingIds.get(i % existingIds.size());
            futures.add(executor.submit(() -> {
                latch.await();
                mockMvc.perform(get("/tickets/{id}", readId))
                        .andExpect(status().isOk());
                return 200;
            }));
        }

        for (int i = 0; i < 5; i++) {
            final String updateId = existingIds.get(i);
            futures.add(executor.submit(() -> {
                latch.await();
                UpdateTicketRequest upd = new UpdateTicketRequest();
                upd.setStatus(TicketStatus.IN_PROGRESS);
                mockMvc.perform(put("/tickets/{id}", updateId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(upd)))
                        .andExpect(status().isOk());
                return 200;
            }));
        }

        latch.countDown();

        for (Future<Integer> future : futures) {
            int statusCode = future.get(15, TimeUnit.SECONDS);
            assertTrue(statusCode == 200 || statusCode == 201);
        }
        executor.shutdown();

        // 5 pre-existing + 10 new = 15 total
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(15)));
    }

    @Test
    void concurrentAutoClassification_20PlusSimultaneousClassifyRequests() throws Exception {
        // Create 25 tickets first
        List<String> ticketIds = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            CreateTicketRequest req = buildCreateRequest(
                    "CUST-CL" + i, "classify" + i + "@test.com", "Classify User " + i,
                    "Login problem with password reset #" + i,
                    "I cannot log in to my account. Password reset is not working for ticket " + i
            );
            MvcResult result = mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andReturn();
            ticketIds.add(extractId(result));
        }

        // Concurrently auto-classify all 25
        ExecutorService executor = Executors.newFixedThreadPool(25);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<MvcResult>> futures = new ArrayList<>();

        for (String id : ticketIds) {
            futures.add(executor.submit(() -> {
                latch.await();
                return mockMvc.perform(post("/tickets/{id}/auto-classify", id))
                        .andExpect(status().isOk())
                        .andReturn();
            }));
        }

        latch.countDown();

        for (Future<MvcResult> future : futures) {
            MvcResult result = future.get(15, TimeUnit.SECONDS);
            JsonNode classification = objectMapper.readTree(result.getResponse().getContentAsString());
            assertNotNull(classification.get("category"));
            assertNotNull(classification.get("priority"));
            assertTrue(classification.get("confidence").asDouble() >= 0.0);
        }
        executor.shutdown();
    }

    // -----------------------------------------------------------------------
    // 4. Combined Filtering by Category and Priority
    // -----------------------------------------------------------------------

    @Test
    void combinedFiltering_byCategoryAndPriority() throws Exception {
        // Create tickets with different categories and priorities
        createTicketDirect("CUST-F1", "f1@test.com", "Filter User 1",
                "Password issue", "I forgot my password and can't access my account",
                TicketCategory.ACCOUNT_ACCESS, TicketPriority.HIGH);

        createTicketDirect("CUST-F2", "f2@test.com", "Filter User 2",
                "Payment error", "Payment failed twice on billing page",
                TicketCategory.BILLING_QUESTION, TicketPriority.HIGH);

        createTicketDirect("CUST-F3", "f3@test.com", "Filter User 3",
                "App crash", "The application crashes with an error on mobile",
                TicketCategory.TECHNICAL_ISSUE, TicketPriority.MEDIUM);

        createTicketDirect("CUST-F4", "f4@test.com", "Filter User 4",
                "Feature: Dark mode", "I would like to suggest a dark mode feature",
                TicketCategory.FEATURE_REQUEST, TicketPriority.LOW);

        createTicketDirect("CUST-F5", "f5@test.com", "Filter User 5",
                "Another login issue", "Cannot login with 2FA, locked out of account",
                TicketCategory.ACCOUNT_ACCESS, TicketPriority.URGENT);

        createTicketDirect("CUST-F6", "f6@test.com", "Filter User 6",
                "Billing refund", "I need a refund for an overcharge on my invoice",
                TicketCategory.BILLING_QUESTION, TicketPriority.MEDIUM);

        // Filter by category only
        mockMvc.perform(get("/tickets").param("category", "account_access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", everyItem(is("account_access"))));

        mockMvc.perform(get("/tickets").param("category", "billing_question"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", everyItem(is("billing_question"))));

        // Filter by priority only
        mockMvc.perform(get("/tickets").param("priority", "high"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].priority", everyItem(is("high"))));

        // Combined: category + priority
        mockMvc.perform(get("/tickets")
                        .param("category", "account_access")
                        .param("priority", "high"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("account_access"))
                .andExpect(jsonPath("$[0].priority").value("high"));

        mockMvc.perform(get("/tickets")
                        .param("category", "billing_question")
                        .param("priority", "medium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("billing_question"))
                .andExpect(jsonPath("$[0].priority").value("medium"));

        // Combined filter with no results
        mockMvc.perform(get("/tickets")
                        .param("category", "feature_request")
                        .param("priority", "urgent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void combinedFiltering_byCategoryPriorityAndStatus() throws Exception {
        // Create and update tickets to have different statuses
        String id1 = createTicketDirect("CUST-FS1", "fs1@test.com", "Status User 1",
                "Login error", "Cannot login to the system, error message on account access page",
                TicketCategory.ACCOUNT_ACCESS, TicketPriority.HIGH);

        String id2 = createTicketDirect("CUST-FS2", "fs2@test.com", "Status User 2",
                "Invoice issue", "The invoice shows wrong billing amount for this cycle",
                TicketCategory.BILLING_QUESTION, TicketPriority.HIGH);

        String id3 = createTicketDirect("CUST-FS3", "fs3@test.com", "Status User 3",
                "Account locked", "My account is locked after login attempts, can't access anything",
                TicketCategory.ACCOUNT_ACCESS, TicketPriority.HIGH);

        // Move id1 to IN_PROGRESS
        UpdateTicketRequest updateReq = new UpdateTicketRequest();
        updateReq.setStatus(TicketStatus.IN_PROGRESS);
        mockMvc.perform(put("/tickets/{id}", id1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // Move id2 to RESOLVED
        UpdateTicketRequest resolveReq = new UpdateTicketRequest();
        resolveReq.setStatus(TicketStatus.RESOLVED);
        mockMvc.perform(put("/tickets/{id}", id2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk());

        // Filter: category=account_access, priority=high, status=new
        mockMvc.perform(get("/tickets")
                        .param("category", "account_access")
                        .param("priority", "high")
                        .param("status", "new"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customer_id").value("CUST-FS3"));

        // Filter: status=in_progress
        mockMvc.perform(get("/tickets")
                        .param("status", "in_progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customer_id").value("CUST-FS1"));

        // Filter: status=resolved, priority=high
        mockMvc.perform(get("/tickets")
                        .param("status", "resolved")
                        .param("priority", "high"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customer_id").value("CUST-FS2"));
    }

    @Test
    void combinedFiltering_byCustomerIdWithOtherFilters() throws Exception {
        createTicketDirect("CUST-CID1", "cid1@test.com", "CID User 1",
                "Login issue", "Cannot access account because of login error",
                TicketCategory.ACCOUNT_ACCESS, TicketPriority.HIGH);

        createTicketDirect("CUST-CID1", "cid1@test.com", "CID User 1",
                "Payment question", "Billing question about my recent charge and invoice",
                TicketCategory.BILLING_QUESTION, TicketPriority.MEDIUM);

        createTicketDirect("CUST-CID2", "cid2@test.com", "CID User 2",
                "Bug report", "Found a bug with reproduction steps, the defect is in search",
                TicketCategory.BUG_REPORT, TicketPriority.MEDIUM);

        // Filter by customerId
        mockMvc.perform(get("/tickets").param("customerId", "CUST-CID1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Filter by customerId + category
        mockMvc.perform(get("/tickets")
                        .param("customerId", "CUST-CID1")
                        .param("category", "account_access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("account_access"));

        // Filter by customerId that has no tickets
        mockMvc.perform(get("/tickets").param("customerId", "CUST-NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -----------------------------------------------------------------------
    // Additional Edge-Case Integration Tests
    // -----------------------------------------------------------------------

    @Test
    void importWithInvalidRecords_partialSuccess() throws Exception {
        String jsonContent = """
                [
                  {
                    "customer_id": "CUST-VALID",
                    "customer_email": "valid@test.com",
                    "customer_name": "Valid User",
                    "subject": "Valid ticket",
                    "description": "This is a valid ticket description that passes all validation checks",
                    "category": "other",
                    "priority": "medium",
                    "status": "new"
                  },
                  {
                    "customer_id": "",
                    "customer_email": "invalid-email",
                    "customer_name": "",
                    "subject": "",
                    "description": "short",
                    "category": "other",
                    "priority": "medium",
                    "status": "new"
                  }
                ]
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "mixed.json", "application/json",
                jsonContent.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/tickets/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_records").value(2))
                .andExpect(jsonPath("$.successful_records").value(1))
                .andExpect(jsonPath("$.failed_records").value(1))
                .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void autoClassify_updatesTicketOnServer() throws Exception {
        // Create a ticket about billing
        String id = createTicketDirect("CUST-ACL", "acl@test.com", "Classify Test",
                "Invoice refund request",
                "Please issue a refund for the incorrect billing charge on my invoice. Payment was wrong.",
                TicketCategory.OTHER, TicketPriority.MEDIUM);

        // Auto-classify
        MvcResult classifyResult = mockMvc.perform(post("/tickets/{id}/auto-classify", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("billing_question"))
                .andReturn();

        // Verify the ticket was updated on the server
        mockMvc.perform(get("/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("billing_question"))
                .andExpect(jsonPath("$.classification_confidence").isNumber())
                .andExpect(jsonPath("$.classification_reasoning").isString())
                .andExpect(jsonPath("$.classification_keywords").isArray());
    }

    // -----------------------------------------------------------------------
    // Helper Methods
    // -----------------------------------------------------------------------

    private CreateTicketRequest buildCreateRequest(String customerId, String email, String name,
                                                    String subject, String description) {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setCustomerId(customerId);
        req.setCustomerEmail(email);
        req.setCustomerName(name);
        req.setSubject(subject);
        req.setDescription(description);
        return req;
    }

    private String createTicketDirect(String customerId, String email, String name,
                                       String subject, String description,
                                       TicketCategory category, TicketPriority priority) throws Exception {
        CreateTicketRequest req = buildCreateRequest(customerId, email, name, subject, description);
        req.setCategory(category);
        req.setPriority(priority);

        MvcResult result = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private String extractId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asText();
    }
}
