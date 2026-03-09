package com.support.ticket.controller;

import com.support.ticket.exception.TicketNotFoundException;
import com.support.ticket.service.ClassificationService;
import com.support.ticket.service.TicketImportService;
import com.support.ticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class ExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private TicketImportService importService;

    @MockBean
    private ClassificationService classificationService;

    @Test
    void handleMalformedJson() throws Exception {
        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void handleInvalidEnumInJson() throws Exception {
        String json = """
            {
                "customerId": "CUST001",
                "customerEmail": "test@example.com",
                "customerName": "Test",
                "subject": "Test Subject",
                "description": "Test description that is valid",
                "category": "invalid_category_value"
            }
            """;

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleGenericException() throws Exception {
        UUID id = UUID.randomUUID();
        when(ticketService.getTicketById(id)).thenThrow(new RuntimeException("unexpected error"));

        mockMvc.perform(get("/tickets/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void handleDeleteNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new TicketNotFoundException(id))
                .when(ticketService).deleteTicket(id);

        mockMvc.perform(delete("/tickets/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
