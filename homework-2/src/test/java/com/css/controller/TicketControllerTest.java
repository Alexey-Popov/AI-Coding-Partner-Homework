package com.css.controller;

import com.css.dto.CreateTicketRequest;
import com.css.dto.ImportResult;
import com.css.dto.UpdateTicketRequest;
import com.css.exception.GlobalExceptionHandler;
import com.css.exception.TicketNotFoundException;
import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import com.css.model.TicketStatus;
import com.css.service.ClassificationResult;
import com.css.service.TicketClassificationService;
import com.css.service.TicketService;
import com.css.service.importer.TicketImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketImportService importService;

    @Mock
    private TicketClassificationService classificationService;

    @InjectMocks
    private TicketController ticketController;

    private Ticket sampleTicket;
    private UUID ticketId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(ticketController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        ticketId = UUID.randomUUID();
        sampleTicket = new Ticket();
        sampleTicket.setId(ticketId);
        sampleTicket.setCustomerId("cust-1");
        sampleTicket.setCustomerEmail("user@example.com");
        sampleTicket.setCustomerName("Test User");
        sampleTicket.setSubject("Cannot login");
        sampleTicket.setDescription("I cannot log in to my account since yesterday.");
        sampleTicket.setCategory(TicketCategory.ACCOUNT_ACCESS);
        sampleTicket.setPriority(TicketPriority.HIGH);
        sampleTicket.setStatus(TicketStatus.NEW);
    }

    // 1. POST /tickets – successful creation returns 201
    @Test
    void createTicket_returnsCreated() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("cust-1");
        request.setCustomerEmail("user@example.com");
        request.setCustomerName("Test User");
        request.setSubject("Cannot login");
        request.setDescription("I cannot log in to my account since yesterday.");

        when(ticketService.createTicket(any(CreateTicketRequest.class))).thenReturn(sampleTicket);

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId", is("cust-1")))
                .andExpect(jsonPath("$.subject", is("Cannot login")));
    }

    // 2. POST /tickets – service throws ValidationException → 400
    @Test
    void createTicket_whenServiceThrowsValidation_returnsBadRequest() throws Exception {
        when(ticketService.createTicket(any(CreateTicketRequest.class)))
                .thenThrow(new com.css.exception.ValidationException("subject is required", java.util.Map.of()));

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // 3. POST /tickets/import – valid CSV file returns 200 with summary
    @Test
    void importTickets_withValidFile_returnsOk() throws Exception {
        ImportResult result = new ImportResult();
        result.setTotalRecords(3);
        result.setSuccessfulRecords(3);
        result.setFailedRecords(0);

        when(importService.importTickets(any())).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets.csv", "text/csv",
                "id,subject\n1,Test".getBytes());

        mockMvc.perform(multipart("/tickets/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords", is(3)))
                .andExpect(jsonPath("$.successfulRecords", is(3)));
    }

    // 4. POST /tickets/import – empty file returns 400
    @Test
    void importTickets_withEmptyFile_returnsBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/tickets/import").file(emptyFile))
                .andExpect(status().isBadRequest());
    }

    // 5. GET /tickets – no filters returns full list with 200
    @Test
    void getAllTickets_noFilters_returnsOkWithList() throws Exception {
        when(ticketService.getAllTickets()).thenReturn(List.of(sampleTicket));

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].subject", is("Cannot login")));
    }

    // 6. GET /tickets?status=NEW – with filter delegates to getFilteredTickets
    @Test
    void getAllTickets_withFilter_returnsFilteredList() throws Exception {
        when(ticketService.getFilteredTickets(null, null, TicketStatus.NEW, null))
                .thenReturn(List.of(sampleTicket));

        mockMvc.perform(get("/tickets").param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("new")));

        verify(ticketService, never()).getAllTickets();
        verify(ticketService).getFilteredTickets(null, null, TicketStatus.NEW, null);
    }

    // 7. GET /tickets/{id} – existing ticket returns 200
    @Test
    void getTicketById_existing_returnsOk() throws Exception {
        when(ticketService.getTicketById(ticketId)).thenReturn(sampleTicket);

        mockMvc.perform(get("/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", is("cust-1")));
    }

    // 8. GET /tickets/{id} – non-existent ticket returns 404
    @Test
    void getTicketById_notFound_returns404() throws Exception {
        UUID unknown = UUID.randomUUID();
        when(ticketService.getTicketById(unknown))
                .thenThrow(new TicketNotFoundException("Ticket not found with id: " + unknown));

        mockMvc.perform(get("/tickets/{id}", unknown))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // 9. PUT /tickets/{id} – successful update returns 200
    @Test
    void updateTicket_existing_returnsOk() throws Exception {
        UpdateTicketRequest updateRequest = new UpdateTicketRequest();
        updateRequest.setSubject("Updated subject");

        Ticket updated = new Ticket();
        updated.setId(ticketId);
        updated.setSubject("Updated subject");
        updated.setStatus(TicketStatus.IN_PROGRESS);

        when(ticketService.updateTicket(eq(ticketId), any(UpdateTicketRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject", is("Updated subject")));
    }

    // 10. DELETE /tickets/{id} – returns 204 No Content
    @Test
    void deleteTicket_existing_returnsNoContent() throws Exception {
        doNothing().when(ticketService).deleteTicket(ticketId);

        mockMvc.perform(delete("/tickets/{id}", ticketId))
                .andExpect(status().isNoContent());

        verify(ticketService).deleteTicket(ticketId);
    }

    // 11. POST /tickets/{id}/auto-classify – returns classification result with 200
    @Test
    void autoClassifyTicket_existing_returnsClassificationResult() throws Exception {
        ClassificationResult result = new ClassificationResult();
        result.setCategory(TicketCategory.ACCOUNT_ACCESS);
        result.setPriority(TicketPriority.HIGH);
        result.setConfidence(0.92);
        result.setReasoning("Keyword 'login' detected");
        result.setKeywords(List.of("login", "access"));

        when(ticketService.getTicketById(ticketId)).thenReturn(sampleTicket);
        when(classificationService.classify(sampleTicket)).thenReturn(result);
        when(ticketService.saveTicket(any(Ticket.class))).thenReturn(sampleTicket);

        mockMvc.perform(post("/tickets/{id}/auto-classify", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category", is("account_access")))
                .andExpect(jsonPath("$.priority", is("high")))
                .andExpect(jsonPath("$.confidence", is(0.92)));
    }
}
