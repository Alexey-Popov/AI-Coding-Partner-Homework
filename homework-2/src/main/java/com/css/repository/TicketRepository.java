package com.css.repository;

import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import com.css.model.TicketStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class TicketRepository {
    private final Map<UUID, Ticket> tickets = new ConcurrentHashMap<>();

    public Ticket save(Ticket ticket) {
        tickets.put(ticket.getId(), ticket);
        return ticket;
    }

    public Optional<Ticket> findById(UUID id) {
        return Optional.ofNullable(tickets.get(id));
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(tickets.values());
    }

    public List<Ticket> findAllFiltered(TicketCategory category, TicketPriority priority,
                                         TicketStatus status, String customerId) {
        return tickets.values().stream()
                .filter(ticket -> category == null || ticket.getCategory() == category)
                .filter(ticket -> priority == null || ticket.getPriority() == priority)
                .filter(ticket -> status == null || ticket.getStatus() == status)
                .filter(ticket -> customerId == null || customerId.equals(ticket.getCustomerId()))
                .collect(Collectors.toList());
    }

    public void deleteById(UUID id) {
        tickets.remove(id);
    }

    public boolean existsById(UUID id) {
        return tickets.containsKey(id);
    }

    public long count() {
        return tickets.size();
    }

    public void deleteAll() {
        tickets.clear();
    }
}

