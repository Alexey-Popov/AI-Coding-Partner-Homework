package com.support.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.model.Ticket;
import com.support.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTest {
    
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
    
    @Test
    void testCompleteTicketLifecycle() throws Exception {
        // Create ticket
        Ticket ticket = createValidTicket();
        String response = mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticket)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        
        Ticket created = objectMapper.readValue(response, Ticket.class);
        String ticketId = created.getId();
        
        // Get ticket
        mockMvc.perform(get("/tickets/" + ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ticketId));
        
        // Auto-classify ticket
        mockMvc.perform(post("/tickets/" + ticketId + "/auto-classify"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confidence").exists());
        
        // Update ticket
        ticket.setStatus(Ticket.Status.RESOLVED);
        mockMvc.perform(put("/tickets/" + ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticket)))
            .andExpect(status().isOk());
        
        // Delete ticket
        mockMvc.perform(delete("/tickets/" + ticketId))
            .andExpect(status().isNoContent());
        
        // Verify deletion
        mockMvc.perform(get("/tickets/" + ticketId))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testBulkImportWithAutoClassification() throws Exception {
        String csvContent = """
            customer_id,customer_email,customer_name,subject,description
            CUST-001,john@example.com,John Doe,Login Issue,Cannot login with password
            CUST-002,jane@example.com,Jane Smith,Bug Report,Application crashes on startup
            """;
        
        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", 
            csvContent.getBytes(StandardCharsets.UTF_8));
        
        mockMvc.perform(multipart("/tickets/import")
                .file(file)
                .param("format", "csv"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRecords").value(2))
            .andExpect(jsonPath("$.successfulRecords").value(2));
        
        // Verify tickets were created
        mockMvc.perform(get("/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    void testConcurrentOperations() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        // Submit 20 concurrent requests
        for (int i = 0; i < 20; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                try {
                    Ticket ticket = createValidTicket();
                    ticket.setCustomerId("CUST-" + index);
                    
                    mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ticket)))
                        .andExpect(status().isCreated());
                    
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }));
        }
        
        // Wait for all requests to complete
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        // Verify all succeeded
        long successCount = futures.stream()
            .map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    return false;
                }
            })
            .filter(success -> success)
            .count();
        
        assertEquals(20, successCount);
        
        // Verify all tickets were created
        List<Ticket> allTickets = ticketRepository.findAll();
        assertEquals(20, allTickets.size());
    }
    
    @Test
    void testFilteringByCategoryAndPriority() throws Exception {
        // Create tickets with different categories and priorities
        createAndSaveTicket(Ticket.Category.TECHNICAL_ISSUE, Ticket.Priority.HIGH);
        createAndSaveTicket(Ticket.Category.TECHNICAL_ISSUE, Ticket.Priority.LOW);
        createAndSaveTicket(Ticket.Category.BILLING_QUESTION, Ticket.Priority.HIGH);
        
        // Filter by category only
        mockMvc.perform(get("/tickets?category=TECHNICAL_ISSUE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
        
        // Filter by priority only
        mockMvc.perform(get("/tickets?priority=HIGH"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
        
        // Filter by both
        mockMvc.perform(get("/tickets?category=TECHNICAL_ISSUE&priority=HIGH"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }
    
    @Test
    void testPerformanceBenchmark() throws Exception {
        long startTime = System.currentTimeMillis();
        
        // Create 100 tickets
        for (int i = 0; i < 100; i++) {
            Ticket ticket = createValidTicket();
            ticket.setCustomerId("CUST-" + i);
            
            mockMvc.perform(post("/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ticket)))
                .andExpect(status().isCreated());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time (10 seconds)
        assertTrue(duration < 10000, "Performance test took too long: " + duration + "ms");
        
        // Verify count
        List<Ticket> allTickets = ticketRepository.findAll();
        assertEquals(100, allTickets.size());
    }
    
    private Ticket createValidTicket() {
        return Ticket.builder()
            .customerId("CUST-001")
            .customerEmail("test@example.com")
            .customerName("Test Customer")
            .subject("Test Issue")
            .description("This is a test description with enough characters")
            .category(Ticket.Category.TECHNICAL_ISSUE)
            .priority(Ticket.Priority.MEDIUM)
            .status(Ticket.Status.NEW)
            .build();
    }
    
    private void createAndSaveTicket(Ticket.Category category, Ticket.Priority priority) 
            throws Exception {
        Ticket ticket = createValidTicket();
        ticket.setCategory(category);
        ticket.setPriority(priority);
        
        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticket)))
            .andExpect(status().isCreated());
    }
}
