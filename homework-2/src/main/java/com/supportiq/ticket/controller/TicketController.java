package com.supportiq.ticket.controller;

import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.dto.request.UpdateTicketRequest;
import com.supportiq.ticket.dto.response.ClassificationResultDto;
import com.supportiq.ticket.dto.response.ImportResultDto;
import com.supportiq.ticket.dto.response.TicketDto;
import com.supportiq.ticket.enums.TicketCategory;
import com.supportiq.ticket.enums.TicketPriority;
import com.supportiq.ticket.enums.TicketStatus;
import com.supportiq.ticket.service.TicketService;
import com.supportiq.ticket.service.imports.ImportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final ImportService importService;

    public TicketController(TicketService ticketService, ImportService importService) {
        this.ticketService = ticketService;
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<TicketDto> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketDto ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicket(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getTicket(id));
    }

    @GetMapping
    public ResponseEntity<Page<TicketDto>> listTickets(
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Instant createdAfter,
            @RequestParam(required = false) Instant createdBefore,
            @RequestParam(required = false) String tag,
            @RequestParam(value = "assigned_to", required = false) String assignedTo,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TicketDto> page = ticketService.listTickets(
                category, priority, status, email, createdAfter, createdBefore, tag, assignedTo, pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketDto> updateTicket(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateTicketRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/auto-classify")
    public ResponseEntity<ClassificationResultDto> classifyTicket(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.classifyTicket(id));
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResultDto> importTickets(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "autoClassify", defaultValue = "false") boolean autoClassify) {
        ImportResultDto result = importService.importTickets(file, autoClassify);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
