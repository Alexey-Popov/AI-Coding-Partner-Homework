package com.supportiq.ticket.api;

import com.supportiq.ticket.BaseIntegrationTest;
import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.dto.request.UpdateTicketRequest;
import com.supportiq.ticket.entity.TicketEntity;
import com.supportiq.ticket.entity.TicketMetadata;
import com.supportiq.ticket.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TicketControllerTest extends BaseIntegrationTest {

    @Test
    void createTicket_validRequest_returns201() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setSubject("Test ticket");
        request.setDescription("This is a test ticket description");
        request.setCategory(TicketCategory.TECHNICAL_ISSUE);
        request.setPriority(TicketPriority.MEDIUM);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customer_name").value("John Doe"))
                .andExpect(jsonPath("$.customer_email").value("john@example.com"))
                .andExpect(jsonPath("$.status").value("new"));
    }

    @Test
    void createTicket_missingRequiredFields_returns400() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void createTicket_invalidEmail_returns400() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerName("John Doe");
        request.setCustomerEmail("not-an-email");
        request.setSubject("Test");
        request.setDescription("Description");

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTicket_existingId_returns200() throws Exception {
        TicketEntity saved = createSampleTicket();

        mockMvc.perform(get("/api/tickets/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.customer_name").value("Jane Doe"));
    }

    @Test
    void getTicket_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/tickets/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listTickets_noFilter_returnsAll() throws Exception {
        createSampleTicket();
        createSampleTicket();

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.total_elements").value(2));
    }

    @Test
    void listTickets_filterByCategory_returnsFiltered() throws Exception {
        TicketEntity ticket = createSampleTicket();
        ticket.setCategory(TicketCategory.BILLING_QUESTION);
        ticketRepository.save(ticket);

        createSampleTicket(); // default TECHNICAL_ISSUE

        mockMvc.perform(get("/api/tickets").param("category", "billing_question"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].category").value("billing_question"));
    }

    @Test
    void updateTicket_partialUpdate_returns200() throws Exception {
        TicketEntity saved = createSampleTicket();

        UpdateTicketRequest update = new UpdateTicketRequest();
        update.setStatus(TicketStatus.IN_PROGRESS);
        update.setPriority(TicketPriority.HIGH);

        mockMvc.perform(put("/api/tickets/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("in_progress"))
                .andExpect(jsonPath("$.priority").value("high"))
                .andExpect(jsonPath("$.customer_name").value("Jane Doe"));
    }

    @Test
    void deleteTicket_existingId_returns204() throws Exception {
        TicketEntity saved = createSampleTicket();

        mockMvc.perform(delete("/api/tickets/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tickets/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTicket_nonExistentId_returns404() throws Exception {
        mockMvc.perform(delete("/api/tickets/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void classifyTicket_existingTicket_returns200WithClassification() throws Exception {
        TicketEntity saved = createSampleTicket();
        saved.setSubject("Cannot login to my account");
        saved.setDescription("My password is not working and I am locked out");
        ticketRepository.save(saved);

        mockMvc.perform(post("/api/tickets/{id}/auto-classify", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").exists())
                .andExpect(jsonPath("$.priority").exists())
                .andExpect(jsonPath("$.confidence").isNumber())
                .andExpect(jsonPath("$.reasoning").isString())
                .andExpect(jsonPath("$.keywords").isArray());
    }

    private TicketEntity createSampleTicket() {
        TicketEntity entity = new TicketEntity();
        entity.setCustomerName("Jane Doe");
        entity.setCustomerEmail("jane@example.com");
        entity.setSubject("Test ticket");
        entity.setDescription("Test description for ticket");
        entity.setCategory(TicketCategory.TECHNICAL_ISSUE);
        entity.setPriority(TicketPriority.MEDIUM);
        entity.setStatus(TicketStatus.NEW);
        entity.setMetadata(new TicketMetadata(Source.WEB_FORM, "Chrome", DeviceType.DESKTOP));
        entity.setTags(Set.of("test"));
        return ticketRepository.save(entity);
    }
}
