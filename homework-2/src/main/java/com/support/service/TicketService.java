package com.support.service;

import com.support.dto.ClassificationResult;
import com.support.exception.TicketNotFoundException;
import com.support.model.Ticket;
import com.support.repository.TicketRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Validated
@Transactional
public class TicketService {
    
    private final TicketRepository ticketRepository;
    private final ClassificationService classificationService;
    
    public TicketService(TicketRepository ticketRepository, 
                        ClassificationService classificationService) {
        this.ticketRepository = ticketRepository;
        this.classificationService = classificationService;
    }
    
    public Ticket createTicket(@Valid Ticket ticket) {
        return ticketRepository.save(ticket);
    }
    
    public Ticket createTicketWithAutoClassification(@Valid Ticket ticket) {
        ClassificationResult result = classificationService.classifyTicket(
            ticket.getSubject(), ticket.getDescription());
        
        ticket.setCategory(result.getCategory());
        ticket.setPriority(result.getPriority());
        ticket.setClassificationConfidence(result.getConfidence());
        
        return ticketRepository.save(ticket);
    }
    
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }
    
    public Ticket getTicketById(String id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + id));
    }
    
    public Ticket updateTicket(String id, @Valid Ticket updatedTicket) {
        Ticket existing = getTicketById(id);
        
        existing.setCustomerId(updatedTicket.getCustomerId());
        existing.setCustomerEmail(updatedTicket.getCustomerEmail());
        existing.setCustomerName(updatedTicket.getCustomerName());
        existing.setSubject(updatedTicket.getSubject());
        existing.setDescription(updatedTicket.getDescription());
        existing.setCategory(updatedTicket.getCategory());
        existing.setPriority(updatedTicket.getPriority());
        existing.setStatus(updatedTicket.getStatus());
        existing.setAssignedTo(updatedTicket.getAssignedTo());
        existing.setTags(updatedTicket.getTags());
        existing.setMetadata(updatedTicket.getMetadata());
        
        if (updatedTicket.getStatus() == Ticket.Status.RESOLVED || 
            updatedTicket.getStatus() == Ticket.Status.CLOSED) {
            existing.setResolvedAt(LocalDateTime.now());
        }
        
        return ticketRepository.save(existing);
    }
    
    public void deleteTicket(String id) {
        if (!ticketRepository.existsById(id)) {
            throw new TicketNotFoundException("Ticket not found: " + id);
        }
        ticketRepository.deleteById(id);
    }
    
    public ClassificationResult autoClassifyTicket(String id) {
        Ticket ticket = getTicketById(id);
        ClassificationResult result = classificationService.classifyTicket(
            ticket.getSubject(), ticket.getDescription());
        
        ticket.setCategory(result.getCategory());
        ticket.setPriority(result.getPriority());
        ticket.setClassificationConfidence(result.getConfidence());
        
        ticketRepository.save(ticket);
        return result;
    }
    
    public List<Ticket> filterTickets(Ticket.Category category, 
                                      Ticket.Priority priority, 
                                      Ticket.Status status) {
        return ticketRepository.findByFilters(category, priority, status);
    }
}
