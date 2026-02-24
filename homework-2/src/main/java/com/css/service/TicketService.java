package com.css.service;

import com.css.dto.CreateTicketRequest;
import com.css.dto.UpdateTicketRequest;
import com.css.exception.TicketNotFoundException;
import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketMetadata;
import com.css.model.TicketPriority;
import com.css.model.TicketStatus;
import com.css.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketValidationService validationService;
    private final TicketClassificationService classificationService;

    public TicketService(TicketRepository ticketRepository, TicketValidationService validationService,
                         TicketClassificationService classificationService) {
        this.ticketRepository = ticketRepository;
        this.validationService = validationService;
        this.classificationService = classificationService;
    }

    public Ticket createTicket(CreateTicketRequest request) {
        validationService.validateCreateRequest(request);

        Ticket ticket = new Ticket();
        ticket.setCustomerId(request.getCustomerId());
        ticket.setCustomerEmail(request.getCustomerEmail());
        ticket.setCustomerName(request.getCustomerName());
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory() != null ? request.getCategory() : TicketCategory.OTHER);
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.NEW);

        if (request.getTags() != null) {
            ticket.setTags(request.getTags());
        }

        if (request.getMetadata() != null) {
            TicketMetadata metadata = new TicketMetadata();
            metadata.setSource(request.getMetadata().getSource());
            metadata.setBrowser(request.getMetadata().getBrowser());
            metadata.setDeviceType(request.getMetadata().getDeviceType());
            ticket.setMetadata(metadata);
        }

        // Optionally run auto-classification on creation
        if (request.getAutoClassify() != null && request.getAutoClassify()) {
            try {
                var res = classificationService.classify(ticket);
                ticket.setCategory(res.getCategory());
                ticket.setPriority(res.getPriority());
                ticket.setClassificationConfidence(res.getConfidence());
                ticket.setClassificationReasoning(res.getReasoning());
                ticket.setClassificationKeywords(res.getKeywords());
            } catch (Exception ex) {
                // classification should not fail creation
            }
        }

        return ticketRepository.save(ticket);
    }

    public Ticket getTicketById(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + id));
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getFilteredTickets(TicketCategory category, TicketPriority priority,
                                           TicketStatus status, String customerId) {
        return ticketRepository.findAllFiltered(category, priority, status, customerId);
    }

    public Ticket updateTicket(UUID id, UpdateTicketRequest request) {
        validationService.validateUpdateRequest(request);

        Ticket ticket = getTicketById(id);

        if (request.getCustomerId() != null) {
            ticket.setCustomerId(request.getCustomerId());
        }
        if (request.getCustomerEmail() != null) {
            ticket.setCustomerEmail(request.getCustomerEmail());
        }
        if (request.getCustomerName() != null) {
            ticket.setCustomerName(request.getCustomerName());
        }
        if (request.getSubject() != null) {
            ticket.setSubject(request.getSubject());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            ticket.setCategory(request.getCategory());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            TicketStatus oldStatus = ticket.getStatus();
            ticket.setStatus(request.getStatus());

            // Set resolved timestamp when status changes to RESOLVED or CLOSED
            if ((request.getStatus() == TicketStatus.RESOLVED || request.getStatus() == TicketStatus.CLOSED)
                    && oldStatus != TicketStatus.RESOLVED && oldStatus != TicketStatus.CLOSED) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
        }
        if (request.getAssignedTo() != null) {
            ticket.setAssignedTo(request.getAssignedTo());
        }
        if (request.getTags() != null) {
            ticket.setTags(request.getTags());
        }
        if (request.getMetadata() != null) {
            TicketMetadata metadata = ticket.getMetadata();
            if (metadata == null) {
                metadata = new TicketMetadata();
            }
            if (request.getMetadata().getSource() != null) {
                metadata.setSource(request.getMetadata().getSource());
            }
            if (request.getMetadata().getBrowser() != null) {
                metadata.setBrowser(request.getMetadata().getBrowser());
            }
            if (request.getMetadata().getDeviceType() != null) {
                metadata.setDeviceType(request.getMetadata().getDeviceType());
            }
            ticket.setMetadata(metadata);
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public void deleteTicket(UUID id) {
        if (!ticketRepository.existsById(id)) {
            throw new TicketNotFoundException("Ticket not found with id: " + id);
        }
        ticketRepository.deleteById(id);
    }

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }
}

