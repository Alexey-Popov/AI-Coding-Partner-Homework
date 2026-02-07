package com.supportiq.ticket.performance;

import com.supportiq.ticket.BaseIntegrationTest;
import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.entity.TicketEntity;
import com.supportiq.ticket.entity.TicketMetadata;
import com.supportiq.ticket.enums.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("performance")
class PerformanceTest extends BaseIntegrationTest {

    @Test
    void createTicket_respondsWithin500ms() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerName("Perf User");
        request.setCustomerEmail("perf@example.com");
        request.setSubject("Performance test");
        request.setDescription("Testing response time");

        long start = System.currentTimeMillis();
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(500);
    }

    @Test
    void listTickets_with100Records_respondsWithin1000ms() throws Exception {
        for (int i = 0; i < 100; i++) {
            TicketEntity entity = new TicketEntity();
            entity.setCustomerName("User " + i);
            entity.setCustomerEmail("user" + i + "@example.com");
            entity.setSubject("Ticket " + i);
            entity.setDescription("Description for ticket " + i);
            entity.setCategory(TicketCategory.TECHNICAL_ISSUE);
            entity.setPriority(TicketPriority.MEDIUM);
            entity.setStatus(TicketStatus.NEW);
            ticketRepository.save(entity);
        }

        long start = System.currentTimeMillis();
        mockMvc.perform(get("/api/tickets").param("size", "20"))
                .andExpect(status().isOk());
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(1000);
    }

    @Test
    void classifyTicket_respondsWithin200ms() throws Exception {
        TicketEntity entity = new TicketEntity();
        entity.setCustomerName("Perf User");
        entity.setCustomerEmail("perf@example.com");
        entity.setSubject("Cannot login to my account urgently");
        entity.setDescription("Password reset not working and I am locked out of my account");
        entity.setStatus(TicketStatus.NEW);
        entity = ticketRepository.save(entity);

        long start = System.currentTimeMillis();
        mockMvc.perform(post("/api/tickets/{id}/auto-classify", entity.getId()))
                .andExpect(status().isOk());
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(200);
    }

    @Test
    void importCsv_5records_respondsWithin2000ms() throws Exception {
        ClassPathResource resource = new ClassPathResource("fixtures/valid_tickets.csv");
        MockMultipartFile file = new MockMultipartFile("file", "valid_tickets.csv",
                "text/csv", resource.getInputStream());

        long start = System.currentTimeMillis();
        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated());
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(2000);
    }

    @Test
    void filteredQuery_withIndexedFields_respondsWithin500ms() throws Exception {
        for (int i = 0; i < 50; i++) {
            TicketEntity entity = new TicketEntity();
            entity.setCustomerName("User " + i);
            entity.setCustomerEmail("user" + i + "@example.com");
            entity.setSubject("Ticket " + i);
            entity.setDescription("Description " + i);
            entity.setCategory(i % 2 == 0 ? TicketCategory.TECHNICAL_ISSUE : TicketCategory.BILLING_QUESTION);
            entity.setPriority(TicketPriority.MEDIUM);
            entity.setStatus(TicketStatus.NEW);
            entity.setTags(Set.of("tag" + (i % 5)));
            ticketRepository.save(entity);
        }

        long start = System.currentTimeMillis();
        mockMvc.perform(get("/api/tickets")
                        .param("category", "technical_issue")
                        .param("status", "new"))
                .andExpect(status().isOk());
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(500);
    }
}
