package com.css.service.importer;

import com.css.dto.ImportResult;
import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import com.css.model.TicketSource;
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
class XmlImportServiceTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketValidationService validationService;

    @InjectMocks
    private XmlImportService xmlImportService;

    private Ticket savedTicket;

    @BeforeEach
    void setUp() {
        savedTicket = new Ticket();
        savedTicket.setId(UUID.randomUUID());
    }

    // -------------------------------------------------------------------------
    // Empty / no tickets
    // -------------------------------------------------------------------------

    @Test
    void importFromXml_emptyTicketsList_returnsTotalRecordsZero() {
        MockMultipartFile file = xmlFile("<tickets></tickets>");

        ImportResult result = xmlImportService.importFromXml(file);

        assertThat(result.getTotalRecords()).isZero();
        verifyNoInteractions(ticketService, validationService);
    }

    // -------------------------------------------------------------------------
    // Successful import
    // -------------------------------------------------------------------------

    @Test
    void importFromXml_singleValidTicket_succeeds() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = xmlFile("<tickets>" + validTicketXml() + "</tickets>");

        ImportResult result = xmlImportService.importFromXml(file);

        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getSuccessfulRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isZero();
        assertThat(result.getImportedTicketIds()).containsExactly(savedTicket.getId().toString());
    }

    @Test
    void importFromXml_validTicket_mapsFieldsCorrectly() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String xml = "<tickets><ticket>" +
                "<customer_id>CUST01</customer_id>" +
                "<customer_email>john@example.com</customer_email>" +
                "<customer_name>John Doe</customer_name>" +
                "<subject>My Subject</subject>" +
                "<description>Sufficient description here</description>" +
                "<category>BILLING_QUESTION</category>" +
                "<priority>HIGH</priority>" +
                "<status>NEW</status>" +
                "<assigned_to>agent1</assigned_to>" +
                "</ticket></tickets>";

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        xmlImportService.importFromXml(xmlFile(xml));
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
    }

    @Test
    void importFromXml_multipleTickets_allCountedCorrectly() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        Ticket t2 = new Ticket();
        t2.setId(UUID.randomUUID());
        when(ticketService.saveTicket(any()))
                .thenReturn(savedTicket)
                .thenReturn(t2);

        MockMultipartFile file = xmlFile("<tickets>" + validTicketXml() + validTicketXml() + "</tickets>");

        ImportResult result = xmlImportService.importFromXml(file);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessfulRecords()).isEqualTo(2);
        assertThat(result.getImportedTicketIds()).hasSize(2);
    }

    // -------------------------------------------------------------------------
    // Tags parsing
    // -------------------------------------------------------------------------

    @Test
    void importFromXml_withTags_parsesTagsCorrectly() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String xml = "<tickets><ticket>" +
                "<customer_id>C1</customer_id>" +
                "<tags><tag>billing</tag><tag>urgent</tag></tags>" +
                "</ticket></tickets>";

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        xmlImportService.importFromXml(xmlFile(xml));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getTags()).containsExactly("billing", "urgent");
    }

    // -------------------------------------------------------------------------
    // Metadata parsing
    // -------------------------------------------------------------------------

    @Test
    void importFromXml_withMetadata_parsesMetadataFields() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String xml = "<tickets><ticket>" +
                "<customer_id>C1</customer_id>" +
                "<metadata>" +
                "<source>WEB_FORM</source>" +
                "<browser>Firefox</browser>" +
                "<device_type>MOBILE</device_type>" +
                "</metadata>" +
                "</ticket></tickets>";

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        xmlImportService.importFromXml(xmlFile(xml));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getMetadata()).isNotNull();
        assertThat(captor.getValue().getMetadata().getSource()).isEqualTo(TicketSource.WEB_FORM);
        assertThat(captor.getValue().getMetadata().getBrowser()).isEqualTo("Firefox");
    }

    @Test
    void importFromXml_withInvalidMetadataSource_defaultsToApi() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String xml = "<tickets><ticket>" +
                "<metadata><source>UNKNOWN</source></metadata>" +
                "</ticket></tickets>";

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        xmlImportService.importFromXml(xmlFile(xml));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getMetadata().getSource()).isEqualTo(TicketSource.API);
    }

    // -------------------------------------------------------------------------
    // Validation failure
    // -------------------------------------------------------------------------

    @Test
    void importFromXml_validationErrors_incrementsFailedAndAddsErrors() {
        when(validationService.validateTicketData(any()))
                .thenReturn(Map.of("subject", "Subject is required"));

        MockMultipartFile file = xmlFile("<tickets>" + validTicketXml() + "</tickets>");

        ImportResult result = xmlImportService.importFromXml(file);

        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isEqualTo(1);
        assertThat(result.getSuccessfulRecords()).isZero();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getRecordNumber()).isEqualTo(1);
        verifyNoInteractions(ticketService);
    }

    @Test
    void importFromXml_mixedRecords_separatesSuccessAndFailure() {
        when(validationService.validateTicketData(any()))
                .thenReturn(Collections.emptyMap())
                .thenReturn(Map.of("email", "invalid"));
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        MockMultipartFile file = xmlFile("<tickets>" + validTicketXml() + validTicketXml() + "</tickets>");

        ImportResult result = xmlImportService.importFromXml(file);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getSuccessfulRecords()).isEqualTo(1);
        assertThat(result.getFailedRecords()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // Enum fallbacks
    // -------------------------------------------------------------------------

    @Test
    void importFromXml_invalidCategory_defaultsToOther() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String xml = "<tickets><ticket><category>INVALID_CAT</category></ticket></tickets>";
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        xmlImportService.importFromXml(xmlFile(xml));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getCategory()).isEqualTo(TicketCategory.OTHER);
    }

    @Test
    void importFromXml_invalidPriority_defaultsToMedium() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String xml = "<tickets><ticket><priority>INVALID_PRI</priority></ticket></tickets>";
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        xmlImportService.importFromXml(xmlFile(xml));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getPriority()).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void importFromXml_invalidStatus_defaultsToNew() {
        when(validationService.validateTicketData(any())).thenReturn(Collections.emptyMap());
        when(ticketService.saveTicket(any())).thenReturn(savedTicket);

        String xml = "<tickets><ticket><status>INVALID_STATUS</status></ticket></tickets>";
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        xmlImportService.importFromXml(xmlFile(xml));
        verify(ticketService).saveTicket(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(TicketStatus.NEW);
    }

    // -------------------------------------------------------------------------
    // Invalid XML
    // -------------------------------------------------------------------------

    @Test
    void importFromXml_malformedXml_throwsRuntimeException() {
        MockMultipartFile file = xmlFile("<tickets><ticket>not closed");

        assertThatThrownBy(() -> xmlImportService.importFromXml(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read XML file");
    }

    @Test
    void importFromXml_ioException_throwsRuntimeException() {
        MockMultipartFile file = mock(MockMultipartFile.class);
        try {
            when(file.getInputStream()).thenThrow(new java.io.IOException("disk error"));
        } catch (java.io.IOException ignored) {}

        assertThatThrownBy(() -> xmlImportService.importFromXml(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read XML file");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private MockMultipartFile xmlFile(String content) {
        return new MockMultipartFile("file", "tickets.xml", "application/xml",
                content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String validTicketXml() {
        return "<ticket>" +
                "<customer_id>C1</customer_id>" +
                "<customer_email>a@b.com</customer_email>" +
                "<customer_name>Name</customer_name>" +
                "<subject>Subject</subject>" +
                "<description>Description here</description>" +
                "</ticket>";
    }
}
