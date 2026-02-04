package com.support.ticket.repository;

import com.support.ticket.model.Category;
import com.support.ticket.model.Priority;
import com.support.ticket.model.Status;
import com.support.ticket.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByCategory(Category category);

    List<Ticket> findByPriority(Priority priority);

    List<Ticket> findByStatus(Status status);

    List<Ticket> findByCategoryAndPriority(Category category, Priority priority);

    List<Ticket> findByCategoryAndPriorityAndStatus(Category category, Priority priority, Status status);

    List<Ticket> findByCategoryAndStatus(Category category, Status status);

    List<Ticket> findByPriorityAndStatus(Priority priority, Status status);
}
