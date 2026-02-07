package com.supportiq.ticket.integration;

import com.supportiq.ticket.BaseIntegrationTest;
import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.dto.request.UpdateTicketRequest;
import com.supportiq.ticket.enums.TicketPriority;
import com.supportiq.ticket.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkflowIntegrationTest extends BaseIntegrationTest {

    @Test
    void fullTicketLifecycle_createClassifyUpdateResolve() throws Exception {
        // 1. Create ticket
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setCustomerName("Workflow User");
        createRequest.setCustomerEmail("workflow@example.com");
        createRequest.setSubject("Cannot login to my account");
        createRequest.setDescription("My password reset is not working and I am locked out");
        createRequest.setTags(Set.of("login", "urgent"));

        MvcResult createResult = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // 2. Classify ticket
        mockMvc.perform(post("/api/tickets/{id}/auto-classify", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("account_access"));

        // 3. Update status to in_progress
        UpdateTicketRequest updateRequest = new UpdateTicketRequest();
        updateRequest.setStatus(TicketStatus.IN_PROGRESS);
        updateRequest.setPriority(TicketPriority.HIGH);

        mockMvc.perform(put("/api/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("in_progress"))
                .andExpect(jsonPath("$.priority").value("high"));

        // 4. Resolve ticket
        UpdateTicketRequest resolveRequest = new UpdateTicketRequest();
        resolveRequest.setStatus(TicketStatus.RESOLVED);

        mockMvc.perform(put("/api/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("resolved"));
    }

    @Test
    void importAndClassifyWorkflow() throws Exception {
        ClassPathResource resource = new ClassPathResource("fixtures/valid_tickets.json");
        MockMultipartFile file = new MockMultipartFile("file", "valid_tickets.json",
                "application/json", resource.getInputStream());

        mockMvc.perform(multipart("/api/tickets/import")
                        .file(file)
                        .param("autoClassify", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successful").value(5));

        // Verify tickets exist and can be listed
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)));
    }

    @Test
    void filteringWorkflow_createAndFilter() throws Exception {
        // Create multiple tickets with different categories
        createTicketViaApi("User 1", "user1@example.com", "Login issue", "Cannot login to account");
        createTicketViaApi("User 2", "user2@example.com", "Billing question", "Wrong charge on invoice");
        createTicketViaApi("User 3", "user3@example.com", "Another login issue", "Password reset broken");

        // Filter by email
        mockMvc.perform(get("/api/tickets").param("email", "user1@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].customer_email").value("user1@example.com"));
    }

    @Test
    void paginationWorkflow() throws Exception {
        for (int i = 0; i < 5; i++) {
            createTicketViaApi("User " + i, "user" + i + "@example.com", "Ticket " + i, "Description " + i);
        }

        mockMvc.perform(get("/api/tickets")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.total_elements").value(5))
                .andExpect(jsonPath("$.total_pages").value(3));
    }

    @Test
    void importUnsupportedFormat_returns415() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "data.txt",
                "text/plain", "some text data".getBytes());

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isUnsupportedMediaType());
    }

    private void createTicketViaApi(String name, String email, String subject, String description) throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerName(name);
        request.setCustomerEmail(email);
        request.setSubject(subject);
        request.setDescription(description);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
