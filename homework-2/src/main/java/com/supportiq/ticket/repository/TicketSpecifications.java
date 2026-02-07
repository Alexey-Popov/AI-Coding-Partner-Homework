package com.supportiq.ticket.repository;

import com.supportiq.ticket.entity.TicketEntity;
import com.supportiq.ticket.enums.TicketCategory;
import com.supportiq.ticket.enums.TicketPriority;
import com.supportiq.ticket.enums.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class TicketSpecifications {

    private TicketSpecifications() {}

    public static Specification<TicketEntity> hasCategory(TicketCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<TicketEntity> hasPriority(TicketPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<TicketEntity> hasStatus(TicketStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<TicketEntity> hasCustomerEmail(String email) {
        return (root, query, cb) -> email == null ? null : cb.equal(root.get("customerEmail"), email);
    }

    public static Specification<TicketEntity> createdAfter(Instant after) {
        return (root, query, cb) -> after == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), after);
    }

    public static Specification<TicketEntity> createdBefore(Instant before) {
        return (root, query, cb) -> before == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), before);
    }

    public static Specification<TicketEntity> hasTag(String tag) {
        return (root, query, cb) -> {
            if (tag == null) return null;
            return cb.isMember(tag, root.get("tags"));
        };
    }

    public static Specification<TicketEntity> hasAssignedTo(String assignedTo) {
        return (root, query, cb) -> assignedTo == null ? null : cb.equal(root.get("assignedTo"), assignedTo);
    }
}
