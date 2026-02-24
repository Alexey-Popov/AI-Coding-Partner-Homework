package com.css.service;

import com.css.dto.CreateTicketRequest;
import com.css.dto.UpdateTicketRequest;
import com.css.exception.TicketNotFoundException;
import com.css.exception.ValidationException;
import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketMetadata;
import com.css.model.TicketPriority;
import com.css.model.TicketStatus;
import com.css.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketValidationService validationService;

    @Mock
    private TicketClassificationService classificationService;

    @InjectMocks
    private TicketService ticketService;

    private CreateTicketRequest basicCreateRequest;

    @BeforeEach
    void setUp() {
        basicCreateRequest = new CreateTicketRequest();
        basicCreateRequest.setCustomerId("customer-1");
        basicCreateRequest.setCustomerEmail("test@example.com");
        basicCreateRequest.setCustomerName("Test User");
        basicCreateRequest.setSubject("Test Subject");
        basicCreateRequest.setDescription("Test description long enough");

        lenient().when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // -------------------------------------------------------------------------
    // createTicket
    // -------------------------------------------------------------------------

    @Test
    void createTicket_populatesFieldsFromRequest() {
        basicCreateRequest.setCategory(TicketCategory.BILLING_QUESTION);
        basicCreateRequest.setPriority(TicketPriority.HIGH);

        Ticket result = ticketService.createTicket(basicCreateRequest);

        assertThat(result.getCustomerId()).isEqualTo("customer-1");
        assertThat(result.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(result.getCustomerName()).isEqualTo("Test User");
        assertThat(result.getSubject()).isEqualTo("Test Subject");
        assertThat(result.getDescription()).isEqualTo("Test description long enough");
        assertThat(result.getCategory()).isEqualTo(TicketCategory.BILLING_QUESTION);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(result.getStatus()).isEqualTo(TicketStatus.NEW);
        verify(validationService).validateCreateRequest(basicCreateRequest);
        verify(ticketRepository).save(result);
    }

    @Test
    void createTicket_defaultsCategoryToOther_whenNull() {
        basicCreateRequest.setCategory(null);

        Ticket result = ticketService.createTicket(basicCreateRequest);

        assertThat(result.getCategory()).isEqualTo(TicketCategory.OTHER);
    }

    @Test
    void createTicket_defaultsPriorityToMedium_whenNull() {
        basicCreateRequest.setPriority(null);

        Ticket result = ticketService.createTicket(basicCreateRequest);

        assertThat(result.getPriority()).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void createTicket_setsTags_whenProvided() {
        List<String> tags = Arrays.asList("tag1", "tag2");
        basicCreateRequest.setTags(tags);

        Ticket result = ticketService.createTicket(basicCreateRequest);

        assertThat(result.getTags()).containsExactly("tag1", "tag2");
    }

    @Test
    void createTicket_setsMetadata_whenProvided() {
        CreateTicketRequest.MetadataRequest meta = new CreateTicketRequest.MetadataRequest();
        meta.setBrowser("Chrome");
        basicCreateRequest.setMetadata(meta);

        Ticket result = ticketService.createTicket(basicCreateRequest);

        assertThat(result.getMetadata()).isNotNull();
        assertThat(result.getMetadata().getBrowser()).isEqualTo("Chrome");
    }

    @Test
    void createTicket_appliesClassification_whenAutoClassifyTrue() {
        basicCreateRequest.setAutoClassify(true);

        ClassificationResult classificationResult = new ClassificationResult();
        classificationResult.setCategory(TicketCategory.TECHNICAL_ISSUE);
        classificationResult.setPriority(TicketPriority.HIGH);
        classificationResult.setConfidence(0.9);
        classificationResult.setReasoning("Matched technical keywords");
        classificationResult.setKeywords(List.of("error"));
        when(classificationService.classify(any(Ticket.class))).thenReturn(classificationResult);

        Ticket result = ticketService.createTicket(basicCreateRequest);

        assertThat(result.getCategory()).isEqualTo(TicketCategory.TECHNICAL_ISSUE);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(result.getClassificationConfidence()).isEqualTo(0.9);
        assertThat(result.getClassificationReasoning()).isEqualTo("Matched technical keywords");
        assertThat(result.getClassificationKeywords()).containsExactly("error");
    }

    @Test
    void createTicket_doesNotFailCreation_whenClassificationThrows() {
        basicCreateRequest.setAutoClassify(true);
        when(classificationService.classify(any(Ticket.class))).thenThrow(new RuntimeException("AI error"));

        Ticket result = ticketService.createTicket(basicCreateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TicketStatus.NEW);
    }

    @Test
    void createTicket_skipsClassification_whenAutoClassifyFalse() {
        basicCreateRequest.setAutoClassify(false);

        ticketService.createTicket(basicCreateRequest);

        verifyNoInteractions(classificationService);
    }

    @Test
    void createTicket_skipsClassification_whenAutoClassifyNull() {
        basicCreateRequest.setAutoClassify(null);

        ticketService.createTicket(basicCreateRequest);

        verifyNoInteractions(classificationService);
    }

    @Test
    void createTicket_throwsValidationException_whenValidationFails() {
        doThrow(new ValidationException("Validation failed", Map.of("subject", "required")))
                .when(validationService).validateCreateRequest(basicCreateRequest);

        assertThatThrownBy(() -> ticketService.createTicket(basicCreateRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Validation failed");
        verify(ticketRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getTicketById
    // -------------------------------------------------------------------------

    @Test
    void getTicketById_returnsTicket_whenFound() {
        UUID id = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setId(id);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void getTicketById_throwsTicketNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketById(id))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    // -------------------------------------------------------------------------
    // getAllTickets
    // -------------------------------------------------------------------------

    @Test
    void getAllTickets_returnsAllTickets() {
        List<Ticket> tickets = Arrays.asList(new Ticket(), new Ticket());
        when(ticketRepository.findAll()).thenReturn(tickets);

        List<Ticket> result = ticketService.getAllTickets();

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllTickets_returnsEmptyList_whenNoTickets() {
        when(ticketRepository.findAll()).thenReturn(Collections.emptyList());

        List<Ticket> result = ticketService.getAllTickets();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getFilteredTickets
    // -------------------------------------------------------------------------

    @Test
    void getFilteredTickets_delegatesToRepository() {
        List<Ticket> expected = List.of(new Ticket());
        when(ticketRepository.findAllFiltered(TicketCategory.BUG_REPORT, TicketPriority.HIGH,
                TicketStatus.NEW, "cust-1")).thenReturn(expected);

        List<Ticket> result = ticketService.getFilteredTickets(
                TicketCategory.BUG_REPORT, TicketPriority.HIGH, TicketStatus.NEW, "cust-1");

        assertThat(result).isSameAs(expected);
    }

    @Test
    void getFilteredTickets_withNullFilters_delegatesToRepository() {
        when(ticketRepository.findAllFiltered(null, null, null, null)).thenReturn(List.of());

        List<Ticket> result = ticketService.getFilteredTickets(null, null, null, null);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // updateTicket
    // -------------------------------------------------------------------------

    @Test
    void updateTicket_updatesAllProvidedFields() {
        UUID id = UUID.randomUUID();
        Ticket existing = new Ticket();
        existing.setId(id);
        existing.setStatus(TicketStatus.NEW);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setCustomerId("new-customer");
        request.setCustomerEmail("new@example.com");
        request.setCustomerName("New Name");
        request.setSubject("New Subject");
        request.setDescription("New detailed description");
        request.setCategory(TicketCategory.FEATURE_REQUEST);
        request.setPriority(TicketPriority.LOW);
        request.setAssignedTo("agent-42");
        request.setTags(List.of("urgent", "vip"));

        Ticket result = ticketService.updateTicket(id, request);

        assertThat(result.getCustomerId()).isEqualTo("new-customer");
        assertThat(result.getCustomerEmail()).isEqualTo("new@example.com");
        assertThat(result.getCustomerName()).isEqualTo("New Name");
        assertThat(result.getSubject()).isEqualTo("New Subject");
        assertThat(result.getDescription()).isEqualTo("New detailed description");
        assertThat(result.getCategory()).isEqualTo(TicketCategory.FEATURE_REQUEST);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.LOW);
        assertThat(result.getAssignedTo()).isEqualTo("agent-42");
        assertThat(result.getTags()).containsExactly("urgent", "vip");
        verify(ticketRepository).save(existing);
    }

    @Test
    void updateTicket_setsResolvedAt_whenStatusChangesToResolved() {
        UUID id = UUID.randomUUID();
        Ticket existing = new Ticket();
        existing.setId(id);
        existing.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.RESOLVED);

        Ticket result = ticketService.updateTicket(id, request);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(result.getResolvedAt()).isNotNull();
        assertThat(result.getResolvedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void updateTicket_setsResolvedAt_whenStatusChangesToClosed() {
        UUID id = UUID.randomUUID();
        Ticket existing = new Ticket();
        existing.setId(id);
        existing.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.CLOSED);

        Ticket result = ticketService.updateTicket(id, request);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.CLOSED);
        assertThat(result.getResolvedAt()).isNotNull();
    }

    @Test
    void updateTicket_doesNotOverwriteResolvedAt_whenAlreadyResolved() {
        UUID id = UUID.randomUUID();
        LocalDateTime originalResolvedAt = LocalDateTime.now().minusDays(1);
        Ticket existing = new Ticket();
        existing.setId(id);
        existing.setStatus(TicketStatus.RESOLVED);
        existing.setResolvedAt(originalResolvedAt);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.CLOSED);

        Ticket result = ticketService.updateTicket(id, request);

        assertThat(result.getResolvedAt()).isEqualTo(originalResolvedAt);
    }

    @Test
    void updateTicket_doesNotSetResolvedAt_whenStatusChangesToInProgress() {
        UUID id = UUID.randomUUID();
        Ticket existing = new Ticket();
        existing.setId(id);
        existing.setStatus(TicketStatus.NEW);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        Ticket result = ticketService.updateTicket(id, request);

        assertThat(result.getResolvedAt()).isNull();
    }

    @Test
    void updateTicket_updatesMetadata_onExistingMetadata() {
        UUID id = UUID.randomUUID();
        Ticket existing = new Ticket();
        existing.setId(id);
        existing.setStatus(TicketStatus.NEW);
        TicketMetadata existingMeta = new TicketMetadata();
        existingMeta.setBrowser("Firefox");
        existing.setMetadata(existingMeta);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(existing));

        CreateTicketRequest.MetadataRequest metaRequest = new CreateTicketRequest.MetadataRequest();
        metaRequest.setBrowser("Chrome");
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setMetadata(metaRequest);

        Ticket result = ticketService.updateTicket(id, request);

        assertThat(result.getMetadata().getBrowser()).isEqualTo("Chrome");
    }

    @Test
    void updateTicket_createsNewMetadata_whenNoneExists() {
        UUID id = UUID.randomUUID();
        Ticket existing = new Ticket();
        existing.setId(id);
        existing.setStatus(TicketStatus.NEW);
        existing.setMetadata(null);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(existing));

        CreateTicketRequest.MetadataRequest metaRequest = new CreateTicketRequest.MetadataRequest();
        metaRequest.setBrowser("Safari");
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setMetadata(metaRequest);

        Ticket result = ticketService.updateTicket(id, request);

        assertThat(result.getMetadata()).isNotNull();
        assertThat(result.getMetadata().getBrowser()).isEqualTo("Safari");
    }

    @Test
    void updateTicket_setsUpdatedAt() {
        UUID id = UUID.randomUUID();
        Ticket existing = new Ticket();
        existing.setId(id);
        existing.setStatus(TicketStatus.NEW);
        LocalDateTime before = existing.getUpdatedAt();
        when(ticketRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setSubject("Updated subject");

        Ticket result = ticketService.updateTicket(id, request);

        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void updateTicket_throwsTicketNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.updateTicket(id, new UpdateTicketRequest()))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateTicket_throwsValidationException_whenValidationFails() {
        UUID id = UUID.randomUUID();
        UpdateTicketRequest request = new UpdateTicketRequest();
        doThrow(new ValidationException("Validation failed", Map.of("customerEmail", "invalid")))
                .when(validationService).validateUpdateRequest(request);

        assertThatThrownBy(() -> ticketService.updateTicket(id, request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Validation failed");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void updateTicket_ignoresNullFields_leavingExistingValues() {
        UUID id = UUID.randomUUID();
        Ticket existing = new Ticket();
        existing.setId(id);
        existing.setStatus(TicketStatus.NEW);
        existing.setCustomerId("original-customer");
        existing.setPriority(TicketPriority.HIGH);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateTicketRequest request = new UpdateTicketRequest();
        // all fields are null – nothing should change

        Ticket result = ticketService.updateTicket(id, request);

        assertThat(result.getCustomerId()).isEqualTo("original-customer");
        assertThat(result.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    // -------------------------------------------------------------------------
    // deleteTicket
    // -------------------------------------------------------------------------

    @Test
    void deleteTicket_deletesExistingTicket() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.existsById(id)).thenReturn(true);

        ticketService.deleteTicket(id);

        verify(ticketRepository).deleteById(id);
    }

    @Test
    void deleteTicket_throwsTicketNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> ticketService.deleteTicket(id))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining(id.toString());
        verify(ticketRepository, never()).deleteById(any());
    }

    // -------------------------------------------------------------------------
    // saveTicket
    // -------------------------------------------------------------------------

    @Test
    void saveTicket_delegatesToRepository() {
        Ticket ticket = new Ticket();
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Ticket result = ticketService.saveTicket(ticket);

        assertThat(result).isSameAs(ticket);
        verify(ticketRepository).save(ticket);
    }
}
