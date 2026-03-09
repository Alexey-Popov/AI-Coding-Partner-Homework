package com.support.ticket.service;

import com.support.ticket.dto.CreateTicketRequest;
import com.support.ticket.dto.TicketResponse;
import com.support.ticket.dto.UpdateTicketRequest;
import com.support.ticket.exception.TicketNotFoundException;
import com.support.ticket.model.*;
import com.support.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId(request.getCustomerId());
        ticket.setCustomerEmail(request.getCustomerEmail());
        ticket.setCustomerName(request.getCustomerName());
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        ticket.setStatus(request.getStatus() != null ? request.getStatus() : Status.NEW);
        ticket.setAssignedTo(request.getAssignedTo());
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
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponse.fromEntity(saved);
    }

    public List<TicketResponse> getAllTickets(Category category, Priority priority, Status status) {
        List<Ticket> tickets;
        if (category != null && priority != null && status != null) {
            tickets = ticketRepository.findByCategoryAndPriorityAndStatus(category, priority, status);
        } else if (category != null && priority != null) {
            tickets = ticketRepository.findByCategoryAndPriority(category, priority);
        } else if (category != null && status != null) {
            tickets = ticketRepository.findByCategoryAndStatus(category, status);
        } else if (priority != null && status != null) {
            tickets = ticketRepository.findByPriorityAndStatus(priority, status);
        } else if (category != null) {
            tickets = ticketRepository.findByCategory(category);
        } else if (priority != null) {
            tickets = ticketRepository.findByPriority(priority);
        } else if (status != null) {
            tickets = ticketRepository.findByStatus(status);
        } else {
            tickets = ticketRepository.findAll();
        }
        return tickets.stream().map(TicketResponse::fromEntity).collect(Collectors.toList());
    }

    public TicketResponse getTicketById(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        return TicketResponse.fromEntity(ticket);
    }

    public Ticket getTicketEntityById(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    public TicketResponse updateTicket(UUID id, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        if (request.getCustomerEmail() != null) ticket.setCustomerEmail(request.getCustomerEmail());
        if (request.getCustomerName() != null) ticket.setCustomerName(request.getCustomerName());
        if (request.getSubject() != null) ticket.setSubject(request.getSubject());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getCategory() != null) ticket.setCategory(request.getCategory());
        if (request.getPriority() != null) ticket.setPriority(request.getPriority());
        if (request.getStatus() != null) {
            Status oldStatus = ticket.getStatus();
            ticket.setStatus(request.getStatus());
            if (request.getStatus() == Status.RESOLVED && oldStatus != Status.RESOLVED) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
        }
        if (request.getAssignedTo() != null) ticket.setAssignedTo(request.getAssignedTo());
        if (request.getTags() != null) ticket.setTags(request.getTags());

        Ticket saved = ticketRepository.save(ticket);
        return TicketResponse.fromEntity(saved);
    }

    public void deleteTicket(UUID id) {
        if (!ticketRepository.existsById(id)) {
            throw new TicketNotFoundException(id);
        }
        ticketRepository.deleteById(id);
    }

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }
}
