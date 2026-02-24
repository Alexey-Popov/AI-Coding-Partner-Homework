package com.acme.css.tickets;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TicketRepository {

    private final Map<UUID, Ticket> store = new ConcurrentHashMap<>();

    public Ticket save(Ticket ticket) {
        if (ticket.getId() == null) ticket.setId(UUID.randomUUID());
        store.put(ticket.getId(), ticket);
        return ticket;
    }

    public Optional<Ticket> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(store.values());
    }

    public void delete(UUID id) {
        store.remove(id);
    }

    public void clear() {
        store.clear();
    }
}
