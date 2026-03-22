package com.support.ticket.service;

import com.support.ticket.dto.*;
import com.support.ticket.exception.TicketNotFoundException;
import com.support.ticket.model.*;
import com.support.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    private TicketRepository ticketRepository;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketRepository = Mockito.mock(TicketRepository.class);
        ticketService = new TicketService(ticketRepository);
    }

    private CreateTicketRequest createRequest() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("CUST001");
        request.setCustomerEmail("test@example.com");
        request.setCustomerName("Test User");
        request.setSubject("Test Subject");
        request.setDescription("Test description that is long enough");
        return request;
    }

    private Ticket createTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setCustomerId("CUST001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject("Test Subject");
        ticket.setDescription("Test description");
        ticket.setPriority(Priority.MEDIUM);
        ticket.setStatus(Status.NEW);
        ticket.setTags(List.of());
        return ticket;
    }

    @Test
    void createTicketWithMetadata() {
        CreateTicketRequest request = createRequest();
        MetadataRequest meta = new MetadataRequest();
        meta.setSource(Source.WEB_FORM);
        meta.setBrowser("Chrome");
        meta.setDeviceType(DeviceType.DESKTOP);
        request.setMetadata(meta);
        request.setTags(List.of("urgent", "frontend"));
        request.setPriority(Priority.HIGH);
        request.setStatus(Status.IN_PROGRESS);

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        TicketResponse response = ticketService.createTicket(request);

        assertNotNull(response);
        assertEquals("CUST001", response.getCustomerId());
        assertNotNull(response.getMetadata());
        assertEquals(Source.WEB_FORM, response.getMetadata().getSource());
        assertEquals("Chrome", response.getMetadata().getBrowser());
        assertEquals(DeviceType.DESKTOP, response.getMetadata().getDeviceType());
    }

    @Test
    void updateTicketWithResolvedStatus() {
        UUID id = UUID.randomUUID();
        Ticket ticket = createTicket();
        ticket.setId(id);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTicketRequest update = new UpdateTicketRequest();
        update.setStatus(Status.RESOLVED);
        update.setCustomerEmail("new@example.com");
        update.setCustomerName("New Name");
        update.setSubject("New Subject");
        update.setDescription("New description that is long enough");
        update.setCategory(Category.TECHNICAL_ISSUE);
        update.setPriority(Priority.HIGH);
        update.setAssignedTo("agent-1");
        update.setTags(List.of("resolved"));

        TicketResponse response = ticketService.updateTicket(id, update);

        assertEquals(Status.RESOLVED, response.getStatus());
        assertNotNull(response.getResolvedAt());
        assertEquals("new@example.com", response.getCustomerEmail());
        assertEquals("New Name", response.getCustomerName());
        assertEquals("New Subject", response.getSubject());
        assertEquals(Category.TECHNICAL_ISSUE, response.getCategory());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals("agent-1", response.getAssignedTo());
    }

    @Test
    void deleteTicket_notFound() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.existsById(id)).thenReturn(false);

        assertThrows(TicketNotFoundException.class, () -> ticketService.deleteTicket(id));
    }

    @Test
    void getAllTickets_filterByCategoryAndStatus() {
        Ticket ticket = createTicket();
        ticket.setCategory(Category.BILLING_QUESTION);
        ticket.setStatus(Status.NEW);
        when(ticketRepository.findByCategoryAndStatus(Category.BILLING_QUESTION, Status.NEW))
                .thenReturn(List.of(ticket));

        List<TicketResponse> result = ticketService.getAllTickets(Category.BILLING_QUESTION, null, Status.NEW);
        assertEquals(1, result.size());
    }

    @Test
    void getAllTickets_filterByPriorityAndStatus() {
        Ticket ticket = createTicket();
        when(ticketRepository.findByPriorityAndStatus(Priority.HIGH, Status.NEW))
                .thenReturn(List.of(ticket));

        List<TicketResponse> result = ticketService.getAllTickets(null, Priority.HIGH, Status.NEW);
        assertEquals(1, result.size());
    }

    @Test
    void getAllTickets_filterByAllThree() {
        Ticket ticket = createTicket();
        when(ticketRepository.findByCategoryAndPriorityAndStatus(
                Category.TECHNICAL_ISSUE, Priority.HIGH, Status.NEW))
                .thenReturn(List.of(ticket));

        List<TicketResponse> result = ticketService.getAllTickets(
                Category.TECHNICAL_ISSUE, Priority.HIGH, Status.NEW);
        assertEquals(1, result.size());
    }

    @Test
    void testEnumConversions() {
        // Source enum
        assertEquals(Source.WEB_FORM, Source.fromValue("web_form"));
        assertEquals(Source.EMAIL, Source.fromValue("email"));
        assertEquals(Source.API, Source.fromValue("api"));
        assertEquals(Source.CHAT, Source.fromValue("chat"));
        assertEquals(Source.PHONE, Source.fromValue("phone"));
        assertEquals("web_form", Source.WEB_FORM.getValue());
        assertThrows(IllegalArgumentException.class, () -> Source.fromValue("invalid"));

        // DeviceType enum
        assertEquals(DeviceType.DESKTOP, DeviceType.fromValue("desktop"));
        assertEquals(DeviceType.MOBILE, DeviceType.fromValue("mobile"));
        assertEquals(DeviceType.TABLET, DeviceType.fromValue("tablet"));
        assertEquals("desktop", DeviceType.DESKTOP.getValue());
        assertThrows(IllegalArgumentException.class, () -> DeviceType.fromValue("invalid"));
    }

    @Test
    void testTicketMetadataGettersSetters() {
        TicketMetadata meta = new TicketMetadata();
        meta.setSource(Source.EMAIL);
        meta.setBrowser("Firefox");
        meta.setDeviceType(DeviceType.MOBILE);

        assertEquals(Source.EMAIL, meta.getSource());
        assertEquals("Firefox", meta.getBrowser());
        assertEquals(DeviceType.MOBILE, meta.getDeviceType());

        TicketMetadata meta2 = new TicketMetadata(Source.CHAT, "Safari", DeviceType.TABLET);
        assertEquals(Source.CHAT, meta2.getSource());
        assertEquals("Safari", meta2.getBrowser());
        assertEquals(DeviceType.TABLET, meta2.getDeviceType());
    }

    @Test
    void testMetadataRequestGettersSetters() {
        MetadataRequest req = new MetadataRequest();
        req.setSource(Source.PHONE);
        req.setBrowser("Edge");
        req.setDeviceType(DeviceType.DESKTOP);

        assertEquals(Source.PHONE, req.getSource());
        assertEquals("Edge", req.getBrowser());
        assertEquals(DeviceType.DESKTOP, req.getDeviceType());
    }

    @Test
    void testTicketResponseMetadataGettersSetters() {
        TicketResponse.MetadataResponse meta = new TicketResponse.MetadataResponse();
        meta.setSource(Source.API);
        meta.setBrowser("Chrome");
        meta.setDeviceType(DeviceType.MOBILE);

        assertEquals(Source.API, meta.getSource());
        assertEquals("Chrome", meta.getBrowser());
        assertEquals(DeviceType.MOBILE, meta.getDeviceType());
    }
}
