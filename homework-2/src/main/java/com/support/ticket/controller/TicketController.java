package com.support.ticket.controller;

import com.support.ticket.dto.*;
import com.support.ticket.model.Category;
import com.support.ticket.model.Priority;
import com.support.ticket.model.Status;
import com.support.ticket.model.Ticket;
import com.support.ticket.service.ClassificationService;
import com.support.ticket.service.TicketImportService;
import com.support.ticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketImportService importService;
    private final ClassificationService classificationService;

    public TicketController(TicketService ticketService, TicketImportService importService,
                            ClassificationService classificationService) {
        this.ticketService = ticketService;
        this.importService = importService;
        this.classificationService = classificationService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @RequestParam(value = "autoClassify", required = false, defaultValue = "false") boolean autoClassify) {
        TicketResponse response = ticketService.createTicket(request);
        if (autoClassify) {
            Ticket ticket = ticketService.getTicketEntityById(response.getId());
            ClassificationResult result = classificationService.classifyAndUpdate(ticket);
            response = ticketService.getTicketById(response.getId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<BulkImportResponse> importTickets(@RequestParam("file") MultipartFile file) {
        BulkImportResponse response = importService.importTickets(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Status status) {
        List<TicketResponse> tickets = ticketService.getAllTickets(category, priority, status);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable UUID id) {
        TicketResponse response = ticketService.getTicketById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request) {
        TicketResponse response = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/auto-classify")
    public ResponseEntity<ClassificationResult> autoClassify(@PathVariable UUID id) {
        Ticket ticket = ticketService.getTicketEntityById(id);
        ClassificationResult result = classificationService.classifyAndUpdate(ticket);
        return ResponseEntity.ok(result);
    }
}
