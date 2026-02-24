package com.acme.css.tickets;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class Ticket {

    private UUID id;

    @NotBlank
    private String customer_id;

    @Email
    @NotBlank
    private String customer_email;

    @NotBlank
    private String customer_name;

    @NotBlank
    @Size(min = 1, max = 200)
    private String subject;

    @NotBlank
    @Size(min = 10, max = 2000)
    private String description;

    private Category category;

    private Priority priority;

    private Status status;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime created_at;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime updated_at;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OffsetDateTime resolved_at;

    private String assigned_to;

    private List<String> tags;

    private Metadata metadata;

    public Ticket() {
        // default
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getCustomer_email() {
        return customer_email;
    }

    public void setCustomer_email(String customer_email) {
        this.customer_email = customer_email;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OffsetDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(OffsetDateTime created_at) {
        this.created_at = created_at;
    }

    public OffsetDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(OffsetDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public OffsetDateTime getResolved_at() {
        return resolved_at;
    }

    public void setResolved_at(OffsetDateTime resolved_at) {
        this.resolved_at = resolved_at;
    }

    public String getAssigned_to() {
        return assigned_to;
    }

    public void setAssigned_to(String assigned_to) {
        this.assigned_to = assigned_to;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
