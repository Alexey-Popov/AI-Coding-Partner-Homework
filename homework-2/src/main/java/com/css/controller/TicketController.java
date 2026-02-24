package com.css.controller;

import com.css.dto.CreateTicketRequest;
import com.css.dto.ImportResult;
import com.css.dto.UpdateTicketRequest;
import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import com.css.model.TicketStatus;
import com.css.service.TicketService;
import com.css.service.importer.TicketImportService;
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

    public TicketController(TicketService ticketService, TicketImportService importService) {
        this.ticketService = ticketService;
        this.importService = importService;
    }

    /**
     * Create a new support ticket
     * POST /tickets
     */
    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    /**
     * Bulk import tickets from CSV/JSON/XML file
     * POST /tickets/import
     */
    @PostMapping("/import")
    public ResponseEntity<ImportResult> importTickets(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        ImportResult result = importService.importTickets(file);
        return ResponseEntity.ok(result);
    }

    /**
     * List all tickets with optional filtering
     * GET /tickets
     */
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets(
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String customerId) {

        List<Ticket> tickets;
        if (category == null && priority == null && status == null && customerId == null) {
            tickets = ticketService.getAllTickets();
        } else {
            tickets = ticketService.getFilteredTickets(category, priority, status, customerId);
        }
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get a specific ticket by ID
     * GET /tickets/:id
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable UUID id) {
        Ticket ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Update an existing ticket
     * PUT /tickets/:id
     */
    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable UUID id,
                                                @RequestBody UpdateTicketRequest request) {
        Ticket ticket = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Delete a ticket
     * DELETE /tickets/:id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}

