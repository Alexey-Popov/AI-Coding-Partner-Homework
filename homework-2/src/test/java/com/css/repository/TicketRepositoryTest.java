package com.css.repository;

import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import com.css.model.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TicketRepositoryTest {

    private TicketRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TicketRepository();
    }

    // -------------------------------------------------------------------------
    // save / findById
    // -------------------------------------------------------------------------

    @Test
    void save_persistsTicketAndReturnsIt() {
        Ticket ticket = newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW);

        Ticket saved = repository.save(ticket);

        assertThat(saved).isSameAs(ticket);
        assertThat(repository.findById(ticket.getId())).contains(ticket);
    }

    @Test
    void save_overwritesExistingTicketWithSameId() {
        Ticket ticket = newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW);
        repository.save(ticket);

        ticket.setStatus(TicketStatus.RESOLVED);
        repository.save(ticket);

        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findById(ticket.getId()).get().getStatus()).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        Optional<Ticket> result = repository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returnsTicketWhenPresent() {
        Ticket ticket = newTicket("c1", TicketCategory.TECHNICAL_ISSUE, TicketPriority.MEDIUM, TicketStatus.IN_PROGRESS);
        repository.save(ticket);

        Optional<Ticket> result = repository.findById(ticket.getId());

        assertThat(result).isPresent().contains(ticket);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void findAll_returnsEmptyListWhenNoTickets() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void findAll_returnsAllSavedTickets() {
        Ticket t1 = newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.LOW, TicketStatus.NEW);
        Ticket t2 = newTicket("c2", TicketCategory.TECHNICAL_ISSUE, TicketPriority.HIGH, TicketStatus.IN_PROGRESS);
        repository.save(t1);
        repository.save(t2);

        List<Ticket> all = repository.findAll();

        assertThat(all).hasSize(2).containsExactlyInAnyOrder(t1, t2);
    }

    // -------------------------------------------------------------------------
    // findAllFiltered
    // -------------------------------------------------------------------------

    @Test
    void findAllFiltered_noFilters_returnsAllTickets() {
        repository.save(newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW));
        repository.save(newTicket("c2", TicketCategory.TECHNICAL_ISSUE, TicketPriority.LOW, TicketStatus.RESOLVED));

        List<Ticket> result = repository.findAllFiltered(null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAllFiltered_byCategory_returnsMatchingOnly() {
        Ticket billing = newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW);
        Ticket technical = newTicket("c2", TicketCategory.TECHNICAL_ISSUE, TicketPriority.LOW, TicketStatus.NEW);
        repository.save(billing);
        repository.save(technical);

        List<Ticket> result = repository.findAllFiltered(TicketCategory.BILLING_QUESTION, null, null, null);

        assertThat(result).containsExactly(billing);
    }

    @Test
    void findAllFiltered_byPriority_returnsMatchingOnly() {
        Ticket high = newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW);
        Ticket low = newTicket("c2", TicketCategory.BILLING_QUESTION, TicketPriority.LOW, TicketStatus.NEW);
        repository.save(high);
        repository.save(low);

        List<Ticket> result = repository.findAllFiltered(null, TicketPriority.HIGH, null, null);

        assertThat(result).containsExactly(high);
    }

    @Test
    void findAllFiltered_byStatus_returnsMatchingOnly() {
        Ticket newTicket = newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW);
        Ticket resolved = newTicket("c2", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.RESOLVED);
        repository.save(newTicket);
        repository.save(resolved);

        List<Ticket> result = repository.findAllFiltered(null, null, TicketStatus.NEW, null);

        assertThat(result).containsExactly(newTicket);
    }

    @Test
    void findAllFiltered_byCustomerId_returnsMatchingOnly() {
        Ticket c1Ticket = newTicket("customer-1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW);
        Ticket c2Ticket = newTicket("customer-2", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW);
        repository.save(c1Ticket);
        repository.save(c2Ticket);

        List<Ticket> result = repository.findAllFiltered(null, null, null, "customer-1");

        assertThat(result).containsExactly(c1Ticket);
    }

    @Test
    void findAllFiltered_multipleFilters_returnsIntersection() {
        Ticket match = newTicket("c1", TicketCategory.TECHNICAL_ISSUE, TicketPriority.URGENT, TicketStatus.IN_PROGRESS);
        Ticket wrongCategory = newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.URGENT, TicketStatus.IN_PROGRESS);
        Ticket wrongPriority = newTicket("c1", TicketCategory.TECHNICAL_ISSUE, TicketPriority.LOW, TicketStatus.IN_PROGRESS);
        Ticket wrongStatus = newTicket("c1", TicketCategory.TECHNICAL_ISSUE, TicketPriority.URGENT, TicketStatus.RESOLVED);
        repository.save(match);
        repository.save(wrongCategory);
        repository.save(wrongPriority);
        repository.save(wrongStatus);

        List<Ticket> result = repository.findAllFiltered(
                TicketCategory.TECHNICAL_ISSUE, TicketPriority.URGENT, TicketStatus.IN_PROGRESS, null);

        assertThat(result).containsExactly(match);
    }

    @Test
    void findAllFiltered_noMatch_returnsEmptyList() {
        repository.save(newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW));

        List<Ticket> result = repository.findAllFiltered(TicketCategory.TECHNICAL_ISSUE, null, null, null);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // deleteById
    // -------------------------------------------------------------------------

    @Test
    void deleteById_removesExistingTicket() {
        Ticket ticket = newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW);
        repository.save(ticket);

        repository.deleteById(ticket.getId());

        assertThat(repository.findById(ticket.getId())).isEmpty();
        assertThat(repository.count()).isZero();
    }

    @Test
    void deleteById_nonExistentId_doesNotThrow() {
        repository.deleteById(UUID.randomUUID()); // should not throw
        assertThat(repository.count()).isZero();
    }

    // -------------------------------------------------------------------------
    // existsById
    // -------------------------------------------------------------------------

    @Test
    void existsById_returnsTrueForSavedTicket() {
        Ticket ticket = newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW);
        repository.save(ticket);

        assertThat(repository.existsById(ticket.getId())).isTrue();
    }

    @Test
    void existsById_returnsFalseForUnknownId() {
        assertThat(repository.existsById(UUID.randomUUID())).isFalse();
    }

    // -------------------------------------------------------------------------
    // count
    // -------------------------------------------------------------------------

    @Test
    void count_returnsZeroInitially() {
        assertThat(repository.count()).isZero();
    }

    @Test
    void count_reflectsSavedAndDeletedTickets() {
        Ticket t1 = newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW);
        Ticket t2 = newTicket("c2", TicketCategory.TECHNICAL_ISSUE, TicketPriority.LOW, TicketStatus.NEW);
        repository.save(t1);
        repository.save(t2);
        assertThat(repository.count()).isEqualTo(2);

        repository.deleteById(t1.getId());
        assertThat(repository.count()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // deleteAll
    // -------------------------------------------------------------------------

    @Test
    void deleteAll_removesEveryTicket() {
        repository.save(newTicket("c1", TicketCategory.BILLING_QUESTION, TicketPriority.HIGH, TicketStatus.NEW));
        repository.save(newTicket("c2", TicketCategory.TECHNICAL_ISSUE, TicketPriority.LOW, TicketStatus.NEW));

        repository.deleteAll();

        assertThat(repository.count()).isZero();
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void deleteAll_onEmptyRepository_doesNotThrow() {
        repository.deleteAll(); // should not throw
        assertThat(repository.count()).isZero();
    }

    // -------------------------------------------------------------------------
    // helper
    // -------------------------------------------------------------------------

    private Ticket newTicket(String customerId, TicketCategory category,
                              TicketPriority priority, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId(customerId);
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        return ticket;
    }
}
