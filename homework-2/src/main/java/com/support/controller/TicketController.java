package com.support.controller;

import com.support.dto.ClassificationResult;
import com.support.dto.ErrorResponse;
import com.support.dto.ImportResult;
import com.support.model.Ticket;
import com.support.service.ImportService;
import com.support.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    
    private final TicketService ticketService;
    private final ImportService importService;
    
    public TicketController(TicketService ticketService, ImportService importService) {
        this.ticketService = ticketService;
        this.importService = importService;
    }
    
    @PostMapping
    public ResponseEntity<Ticket> createTicket(
            @Valid @RequestBody Ticket ticket,
            @RequestParam(required = false, defaultValue = "false") boolean autoClassify) {
        
        Ticket created = autoClassify 
            ? ticketService.createTicketWithAutoClassification(ticket)
            : ticketService.createTicket(ticket);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PostMapping("/import")
    public ResponseEntity<ImportResult> importTickets(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) {
        
        try {
            ImportResult result = importService.importFile(file, format);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ImportResult.builder()
                    .totalRecords(0)
                    .successfulRecords(0)
                    .failedRecords(0)
                    .errors(List.of(ImportResult.ImportError.builder()
                        .lineNumber(0)
                        .recordData("")
                        .errorMessage(e.getMessage())
                        .build()))
                    .build()
            );
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets(
            @RequestParam(required = false) Ticket.Category category,
            @RequestParam(required = false) Ticket.Priority priority,
            @RequestParam(required = false) Ticket.Status status) {
        
        List<Ticket> tickets = (category != null || priority != null || status != null)
            ? ticketService.filterTickets(category, priority, status)
            : ticketService.getAllTickets();
        
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable String id) {
        Ticket ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(
            @PathVariable String id,
            @Valid @RequestBody Ticket ticket) {
        
        Ticket updated = ticketService.updateTicket(id, ticket);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/auto-classify")
    public ResponseEntity<ClassificationResult> autoClassify(@PathVariable String id) {
        ClassificationResult result = ticketService.autoClassifyTicket(id);
        return ResponseEntity.ok(result);
    }
}
