package com.support.repository;

import com.support.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    
    List<Ticket> findByCategory(Ticket.Category category);
    
    List<Ticket> findByPriority(Ticket.Priority priority);
    
    List<Ticket> findByStatus(Ticket.Status status);
    
    List<Ticket> findByCategoryAndPriority(Ticket.Category category, Ticket.Priority priority);
    
    List<Ticket> findByCustomerId(String customerId);
    
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:status IS NULL OR t.status = :status)")
    List<Ticket> findByFilters(
        @Param("category") Ticket.Category category,
        @Param("priority") Ticket.Priority priority,
        @Param("status") Ticket.Status status
    );
}
