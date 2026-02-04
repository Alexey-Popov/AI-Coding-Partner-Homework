package com.support.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.ticket.dto.*;
import com.support.ticket.exception.TicketNotFoundException;
import com.support.ticket.model.*;
import com.support.ticket.service.ClassificationService;
import com.support.ticket.service.TicketImportService;
import com.support.ticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private TicketImportService importService;

    @MockBean
    private ClassificationService classificationService;

    private TicketResponse createSampleResponse() {
        TicketResponse response = new TicketResponse();
        response.setId(UUID.randomUUID());
        response.setCustomerId("CUST001");
        response.setCustomerEmail("john@example.com");
        response.setCustomerName("John Smith");
        response.setSubject("Test Subject");
        response.setDescription("Test description for the ticket");
        response.setCategory(Category.TECHNICAL_ISSUE);
        response.setPriority(Priority.MEDIUM);
        response.setStatus(Status.NEW);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        response.setTags(List.of());
        return response;
    }

    private String createValidRequestJson() {
        return """
            {
                "customerId": "CUST001",
                "customerEmail": "john@example.com",
                "customerName": "John Smith",
                "subject": "Test Subject",
                "description": "Test description that is long enough to pass validation"
            }
            """;
    }

    @Test
    void createTicket_validInput_returns201() throws Exception {
        TicketResponse response = createSampleResponse();
        when(ticketService.createTicket(any(CreateTicketRequest.class))).thenReturn(response);

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createValidRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("CUST001"))
                .andExpect(jsonPath("$.customerEmail").value("john@example.com"));
    }

    @Test
    void createTicket_missingRequiredFields_returns400() throws Exception {
        String json = """
            {
                "customerId": "CUST001"
            }
            """;

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createTicket_invalidEmail_returns400() throws Exception {
        String json = """
            {
                "customerId": "CUST001",
                "customerEmail": "invalid-email",
                "customerName": "John Smith",
                "subject": "Test Subject",
                "description": "Test description that is long enough to pass validation"
            }
            """;

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllTickets_returns200WithList() throws Exception {
        when(ticketService.getAllTickets(null, null, null))
                .thenReturn(List.of(createSampleResponse()));

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].customerId").value("CUST001"));
    }

    @Test
    void getAllTickets_filterByCategory() throws Exception {
        when(ticketService.getAllTickets(eq(Category.TECHNICAL_ISSUE), isNull(), isNull()))
                .thenReturn(List.of(createSampleResponse()));

        mockMvc.perform(get("/tickets").param("category", "technical_issue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("technical_issue"));
    }

    @Test
    void getAllTickets_filterByPriority() throws Exception {
        TicketResponse response = createSampleResponse();
        response.setPriority(Priority.HIGH);
        when(ticketService.getAllTickets(isNull(), eq(Priority.HIGH), isNull()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/tickets").param("priority", "high"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("high"));
    }

    @Test
    void getTicketById_exists_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        TicketResponse response = createSampleResponse();
        response.setId(id);
        when(ticketService.getTicketById(id)).thenReturn(response);

        mockMvc.perform(get("/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getTicketById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(ticketService.getTicketById(id)).thenThrow(new TicketNotFoundException(id));

        mockMvc.perform(get("/tickets/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateTicket_valid_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        TicketResponse response = createSampleResponse();
        response.setId(id);
        response.setSubject("Updated Subject");
        when(ticketService.updateTicket(eq(id), any(UpdateTicketRequest.class))).thenReturn(response);

        String json = """
            {
                "subject": "Updated Subject"
            }
            """;

        mockMvc.perform(put("/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Updated Subject"));
    }

    @Test
    void updateTicket_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(ticketService.updateTicket(eq(id), any(UpdateTicketRequest.class)))
                .thenThrow(new TicketNotFoundException(id));

        String json = """
            {
                "subject": "Updated Subject"
            }
            """;

        mockMvc.perform(put("/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTicket_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(ticketService).deleteTicket(id);

        mockMvc.perform(delete("/tickets/{id}", id))
                .andExpect(status().isNoContent());
    }
}
