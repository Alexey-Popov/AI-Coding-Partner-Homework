package com.css.service.importer;

import com.css.dto.ImportResult;
import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import com.css.model.TicketStatus;
import com.css.service.TicketService;
import com.css.service.TicketValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonImportServiceTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketValidationService validationService;

    @InjectMocks
    private JsonImportService jsonImportService;

    private Ticket savedTicket;

    @BeforeEach
    void setUp() {
        savedTicket = new Ticket();
        savedTicket.setId(UUID.randomUUID());
    }

    // -------------------------------------------------------------------------
    // Empty / minimal input
    // -------------------------------------------------------------------------

    @Test
    void importFromJson_emptyArray_returnsZeroTotalRecords() {
        MockMultipartFile file = jsonFile("[]");

        ImportResult result = jsonImportService.importFromJson(file);

        assertThat(result.getTotalRecords()).isZero();
        assertThat(result.getSuccessfulRecords()).isZero();
        verifyNoInteractions(ticketService, validationService);
    }

    // -------------------------------------------------------------------------
    // Successful import
    // -------------------------------------------------------------------------

    @Test
    void importFromJson_singleValidRecord_succeeds() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = jsonFile("[" + validJsonTicket() + "]");

        ImportResult result = jsonImportService.importFromJson(file);

        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getSuccessfulRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isZero();
        assertThat(result.getImportedTicketIds()).containsExactly(savedTicket.getId().toString());
    }

    @Test
    void importFromJson_validRecord_mapsFieldsCorrectly() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String json = "[{" +
                "\"customer_id\":\"CUST01\"," +
                "\"customer_email\":\"john@example.com\"," +
                "\"customer_name\":\"John Doe\"," +
                "\"subject\":\"My Subject\"," +
                "\"description\":\"Sufficient description here\"," +
                "\"category\":\"BILLING_QUESTION\"," +
                "\"priority\":\"HIGH\"," +
                "\"status\":\"NEW\"," +
                "\"assigned_to\":\"agent1\"," +
                "\"tags\":[\"tag1\",\"tag2\"]" +
                "}]";

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        jsonImportService.importFromJson(jsonFile(json));
        verify(ticketService).saveTicket(captor.capture());
        Ticket ticket = captor.getValue();

        assertThat(ticket.getCustomerId()).isEqualTo("CUST01");
        assertThat(ticket.getCustomerEmail()).isEqualTo("john@example.com");
        assertThat(ticket.getCustomerName()).isEqualTo("John Doe");
        assertThat(ticket.getSubject()).isEqualTo("My Subject");
        assertThat(ticket.getDescription()).isEqualTo("Sufficient description here");
        assertThat(ticket.getCategory()).isEqualTo(TicketCategory.BILLING_QUESTION);
        assertThat(ticket.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.NEW);
        assertThat(ticket.getAssignedTo()).isEqualTo("agent1");
        assertThat(ticket.getTags()).containsExactly("tag1", "tag2");
    }

    @Test
    void importFromJson_multipleRecords_allCountedCorrectly() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        Ticket t2 = new Ticket();
        t2.setId(UUID.randomUUID());
        when(ticketService.saveTicket(any()))
                .thenReturn(savedTicket)
                .thenReturn(t2);

        MockMultipartFile file = jsonFile("[" + validJsonTicket() + "," + validJsonTicket() + "]");

        ImportResult result = jsonImportService.importFromJson(file);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessfulRecords()).isEqualTo(2);
        assertThat(result.getImportedTicketIds()).hasSize(2);
    }

    // -------------------------------------------------------------------------
    // Validation failure
    // -------------------------------------------------------------------------

    @Test
    void importFromJson_validationErrors_incrementsFailedAndAddsErrors() {
        when(validationService.validateTicketData(any()))
                .thenReturn(Map.of("subject", "Subject is required"));

        MockMultipartFile file = jsonFile("[" + validJsonTicket() + "]");

        ImportResult result = jsonImportService.importFromJson(file);

        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isEqualTo(1);
        assertThat(result.getSuccessfulRecords()).isZero();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getField()).isEqualTo("subject");
        verifyNoInteractions(ticketService);
    }

    @Test
    void importFromJson_mixedRecords_separatesSuccessAndFailure() {
        when(validationService.validateTicketData(any()))
                .thenReturn(Collections.emptyMap())
                .thenReturn(Map.of("email", "invalid"));
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = jsonFile("[" + validJsonTicket() + "," + validJsonTicket() + "]");

        ImportResult result = jsonImportService.importFromJson(file);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessfulRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // Enum fallbacks
    // -------------------------------------------------------------------------

    @Test
    void importFromJson_invalidCategory_defaultsToOther() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String json = "[{\"customer_id\":\"C1\",\"category\":\"TOTALLY_UNKNOWN\"}]";
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        jsonImportService.importFromJson(jsonFile(json));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getCategory()).isEqualTo(TicketCategory.OTHER);
    }

    @Test
    void importFromJson_nullCategory_defaultsToOther() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String json = "[{\"customer_id\":\"C1\"}]";
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        jsonImportService.importFromJson(jsonFile(json));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getCategory()).isEqualTo(TicketCategory.OTHER);
    }

    @Test
    void importFromJson_invalidPriority_defaultsToMedium() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String json = "[{\"priority\":\"INVALID_PRI\"}]";
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        jsonImportService.importFromJson(jsonFile(json));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getPriority()).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void importFromJson_invalidStatus_defaultsToNew() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String json = "[{\"status\":\"INVALID_STATUS\"}]";
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        jsonImportService.importFromJson(jsonFile(json));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(TicketStatus.NEW);
    }

    // -------------------------------------------------------------------------
    // Metadata
    // -------------------------------------------------------------------------

    @Test
    void importFromJson_withMetadata_mapsMetadataFields() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String json = "[{\"metadata\":{\"source\":\"WEB_FORM\",\"browser\":\"Chrome\",\"device_type\":\"MOBILE\"}}]";
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        jsonImportService.importFromJson(jsonFile(json));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getMetadata()).isNotNull();
        assertThat(captor.getValue().getMetadata().getBrowser()).isEqualTo("Chrome");
    }

    @Test
    void importFromJson_withInvalidMetadataSource_defaultsToApi() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String json = "[{\"metadata\":{\"source\":\"UNKNOWN_SRC\"}}]";
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        jsonImportService.importFromJson(jsonFile(json));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getMetadata().getSource())
                .isEqualTo(com.css.model.TicketSource.API);
    }

    // -------------------------------------------------------------------------
    // Invalid JSON
    // -------------------------------------------------------------------------

    @Test
    void importFromJson_invalidJson_throwsRuntimeException() {
        MockMultipartFile file = jsonFile("not json at all");

        assertThatThrownBy(() -> jsonImportService.importFromJson(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read JSON file");
    }

    @Test
    void importFromJson_ioException_throwsRuntimeException() {
        MockMultipartFile file = mock(MockMultipartFile.class);
        try {
            when(file.getInputStream()).thenThrow(new java.io.IOException("disk error"));
        } catch (java.io.IOException ignored) {}

        assertThatThrownBy(() -> jsonImportService.importFromJson(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read JSON file");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private MockMultipartFile jsonFile(String content) {
        return new MockMultipartFile("file", "tickets.json", "application/json",
                content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String validJsonTicket() {
        return "{\"customer_id\":\"C1\",\"customer_email\":\"a@b.com\",\"customer_name\":\"Name\"," +
               "\"subject\":\"Subject\",\"description\":\"Description here\"}";
    }
}
