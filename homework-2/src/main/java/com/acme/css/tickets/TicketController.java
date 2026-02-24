package com.acme.css.tickets;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@Validated
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket ticket,
                                               @RequestParam(name = "auto_classify", required = false) boolean autoClassify) {
        // optional auto-classify handled elsewhere; for now just create
        Ticket created = service.create(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importTickets(@RequestPart("file") MultipartFile file) {
        try {
            TicketService.ImportResult r = service.importFile(file);
            return ResponseEntity.status(HttpStatus.OK).body(r);
        } catch (IllegalArgumentException ia) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ia.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<Ticket> listTickets(@RequestParam(name = "category", required = false) String category,
                                    @RequestParam(name = "priority", required = false) String priority) {
        return service.list(Optional.ofNullable(category), Optional.ofNullable(priority));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTicket(@PathVariable("id") UUID id) {
        Optional<Ticket> ticket = service.get(id);
        if (ticket.isPresent()) {
            return ResponseEntity.ok(ticket.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not found"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable("id") UUID id, @RequestBody Ticket ticket) {
        try {
            Ticket updated = service.update(id, ticket);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not found"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable("id") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
