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
class CsvImportServiceTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketValidationService validationService;

    @InjectMocks
    private CsvImportService csvImportService;

    private static final String HEADER =
            "customer_id,customer_email,customer_name,subject,description,category,priority,status,assigned_to,tags,source,browser,device_type";

    private Ticket savedTicket;

    @BeforeEach
    void setUp() {
        savedTicket = new Ticket();
        savedTicket.setId(UUID.randomUUID());
    }

    // -------------------------------------------------------------------------
    // Empty file / header-only
    // -------------------------------------------------------------------------

    @Test
    void importFromCsv_emptyFile_returnsTotalRecordsZero() {
        MockMultipartFile file = csvFile("");

        ImportResult result = csvImportService.importFromCsv(file);

        assertThat(result.getTotalRecords()).isZero();
        assertThat(result.getSuccessfulRecords()).isZero();
        verifyNoInteractions(ticketService, validationService);
    }

    @Test
    void importFromCsv_headerOnly_returnsTotalRecordsZero() {
        MockMultipartFile file = csvFile(HEADER);

        ImportResult result = csvImportService.importFromCsv(file);

        assertThat(result.getTotalRecords()).isZero();
        verifyNoInteractions(ticketService, validationService);
    }

    // -------------------------------------------------------------------------
    // Successful import
    // -------------------------------------------------------------------------

    @Test
    void importFromCsv_validRecord_incrementsSuccessfulRecords() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = csvFile(HEADER + "\n" + validCsvRow());

        ImportResult result = csvImportService.importFromCsv(file);

        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getSuccessfulRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isZero();
        assertThat(result.getImportedTicketIds()).containsExactly(savedTicket.getId().toString());
    }

    @Test
    void importFromCsv_validRecord_passesCorrectFieldsToTicketService() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = csvFile(HEADER + "\n" +
                "CUST01,john@example.com,John Doe,My Subject,Sufficient description here,billing_question,high,new,agent1,tag1|tag2,web_form,Chrome,desktop");

        csvImportService.importFromCsv(file);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
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
    void importFromCsv_multipleValidRecords_allSucceed() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        Ticket second = new Ticket();
        second.setId(UUID.randomUUID());
        when(ticketService.saveTicket(any()))
                .thenReturn(savedTicket)
                .thenReturn(second);

        MockMultipartFile file = csvFile(HEADER + "\n" + validCsvRow() + "\n" + validCsvRow());

        ImportResult result = csvImportService.importFromCsv(file);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessfulRecords()).isEqualTo(2);
        assertThat(result.getImportedTicketIds()).hasSize(2);
    }

    // -------------------------------------------------------------------------
    // Validation failure
    // -------------------------------------------------------------------------

    @Test
    void importFromCsv_validationErrors_incrementsFailedAndAddsErrors() {
        when(validationService.validateTicketData(any()))
                .thenReturn(Map.of("subject", "Subject is required"));

        MockMultipartFile file = csvFile(HEADER + "\n" + validCsvRow());

        ImportResult result = csvImportService.importFromCsv(file);

        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isEqualTo(1);
        assertThat(result.getSuccessfulRecords()).isZero();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getField()).isEqualTo("subject");
        verifyNoInteractions(ticketService);
    }

    @Test
    void importFromCsv_mixedRecords_separatesSuccessAndFailure() {
        Ticket saved1 = new Ticket();
        saved1.setId(UUID.randomUUID());
        when(validationService.validateTicketData(any()))
                .thenReturn(Collections.emptyMap())
                .thenReturn(Map.of("email", "invalid"));
        when(ticketService.saveTicket(any())).thenReturn(saved1);

        MockMultipartFile file = csvFile(HEADER + "\n" + validCsvRow() + "\n" + validCsvRow());

        ImportResult result = csvImportService.importFromCsv(file);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessfulRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // Enum fallbacks
    // -------------------------------------------------------------------------

    @Test
    void importFromCsv_invalidCategoryValue_defaultsToOther() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = csvFile(HEADER + "\n" +
                "C1,a@b.com,Name,Subject,Description here,INVALID_CAT,medium,new,,,,,");

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        csvImportService.importFromCsv(file);
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getCategory()).isEqualTo(TicketCategory.OTHER);
    }

    @Test
    void importFromCsv_invalidPriorityValue_defaultsToMedium() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = csvFile(HEADER + "\n" +
                "C1,a@b.com,Name,Subject,Description here,other,INVALID_PRI,new,,,,,");

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        csvImportService.importFromCsv(file);
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getPriority()).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void importFromCsv_invalidStatusValue_defaultsToNew() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = csvFile(HEADER + "\n" +
                "C1,a@b.com,Name,Subject,Description here,other,medium,INVALID_STATUS,,,,,");

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        csvImportService.importFromCsv(file);
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(TicketStatus.NEW);
    }

    @Test
    void importFromCsv_emptyCategoryValue_defaultsToOther() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = csvFile(HEADER + "\n" +
                "C1,a@b.com,Name,Subject,Description here,,medium,new,,,,,");

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        csvImportService.importFromCsv(file);
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getCategory()).isEqualTo(TicketCategory.OTHER);
    }

    // -------------------------------------------------------------------------
    // Quoted CSV values
    // -------------------------------------------------------------------------

    @Test
    void importFromCsv_quotedFieldWithComma_parsedCorrectly() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = csvFile(HEADER + "\n" +
                "C1,a@b.com,\"Doe, Jane\",Subject,Description here,other,medium,new,,,,,");

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        csvImportService.importFromCsv(file);
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getCustomerName()).isEqualTo("Doe, Jane");
    }

    @Test
    void importFromCsv_quotedFieldWithEscapedQuote_parsedCorrectly() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = csvFile(HEADER + "\n" +
                "C1,a@b.com,Name,\"Subject with \"\"quotes\"\"\",Description here,other,medium,new,,,,,");

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        csvImportService.importFromCsv(file);
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getSubject()).isEqualTo("Subject with \"quotes\"");
    }

    // -------------------------------------------------------------------------
    // IO error
    // -------------------------------------------------------------------------

    @Test
    void importFromCsv_ioException_throwsRuntimeException() {
        MockMultipartFile file = mock(MockMultipartFile.class);
        try {
            when(file.getInputStream()).thenThrow(new java.io.IOException("disk error"));
        } catch (java.io.IOException ignored) {}

        assertThatThrownBy(() -> csvImportService.importFromCsv(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read CSV file");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private MockMultipartFile csvFile(String content) {
        return new MockMultipartFile("file", "tickets.csv", "text/csv",
                content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String validCsvRow() {
        return "CUST01,john@example.com,John Doe,My Subject,Sufficient description,other,medium,new,,,,,";
    }
}
