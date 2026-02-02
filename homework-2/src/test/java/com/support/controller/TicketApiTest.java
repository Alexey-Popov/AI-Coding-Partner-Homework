package com.support.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.model.Ticket;
import com.support.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
    private com.support.service.ImportService importService;
    
    @Test
    void testCreateTicket_Success() throws Exception {
        Ticket ticket = createValidTicket();
        when(ticketService.createTicket(any(Ticket.class))).thenReturn(ticket);
        
        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticket)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(ticket.getId()))
            .andExpect(jsonPath("$.subject").value(ticket.getSubject()));
    }
    
    @Test
    void testCreateTicket_ValidationError_EmptySubject() throws Exception {
        Ticket ticket = createValidTicket();
        ticket.setSubject("");
        
        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticket)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateTicket_ValidationError_InvalidEmail() throws Exception {
        Ticket ticket = createValidTicket();
        ticket.setCustomerEmail("invalid-email");
        
        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticket)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void testCreateTicket_WithAutoClassify() throws Exception {
        Ticket ticket = createValidTicket();
        when(ticketService.createTicketWithAutoClassification(any(Ticket.class)))
            .thenReturn(ticket);
        
        mockMvc.perform(post("/tickets?autoClassify=true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticket)))
            .andExpect(status().isCreated());
        
        verify(ticketService).createTicketWithAutoClassification(any(Ticket.class));
    }
    
    @Test
    void testGetAllTickets() throws Exception {
        List<Ticket> tickets = Arrays.asList(createValidTicket(), createValidTicket());
        when(ticketService.getAllTickets()).thenReturn(tickets);
        
        mockMvc.perform(get("/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    void testGetTicketById_Found() throws Exception {
        Ticket ticket = createValidTicket();
        when(ticketService.getTicketById("123")).thenReturn(ticket);
        
        mockMvc.perform(get("/tickets/123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ticket.getId()));
    }
    
    @Test
    void testGetTicketById_NotFound() throws Exception {
        when(ticketService.getTicketById("999"))
            .thenThrow(new com.support.exception.TicketNotFoundException("Not found"));
        
        mockMvc.perform(get("/tickets/999"))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testUpdateTicket() throws Exception {
        Ticket ticket = createValidTicket();
        when(ticketService.updateTicket(eq("123"), any(Ticket.class))).thenReturn(ticket);
        
        mockMvc.perform(put("/tickets/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticket)))
            .andExpect(status().isOk());
    }
    
    @Test
    void testDeleteTicket() throws Exception {
        doNothing().when(ticketService).deleteTicket("123");
        
        mockMvc.perform(delete("/tickets/123"))
            .andExpect(status().isNoContent());
    }
    
    @Test
    void testFilterTickets_ByCategory() throws Exception {
        List<Ticket> tickets = List.of(createValidTicket());
        when(ticketService.filterTickets(any(), any(), any())).thenReturn(tickets);
        
        mockMvc.perform(get("/tickets?category=TECHNICAL_ISSUE"))
            .andExpect(status().isOk());
    }
    
    @Test
    void testAutoClassifyTicket() throws Exception {
        com.support.dto.ClassificationResult result = 
            com.support.dto.ClassificationResult.builder()
                .category(Ticket.Category.TECHNICAL_ISSUE)
                .priority(Ticket.Priority.HIGH)
                .confidence(0.85)
                .reasoning("Based on keywords")
                .keywordsFound(List.of("bug", "error"))
                .build();
        
        when(ticketService.autoClassifyTicket("123")).thenReturn(result);
        
        mockMvc.perform(post("/tickets/123/auto-classify"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confidence").value(0.85));
    }
    
    private Ticket createValidTicket() {
        return Ticket.builder()
            .id("test-123")
            .customerId("CUST-001")
            .customerEmail("test@example.com")
            .customerName("Test Customer")
            .subject("Test Issue")
            .description("This is a test description with more than 10 characters")
            .category(Ticket.Category.TECHNICAL_ISSUE)
            .priority(Ticket.Priority.MEDIUM)
            .status(Ticket.Status.NEW)
            .build();
    }
}
