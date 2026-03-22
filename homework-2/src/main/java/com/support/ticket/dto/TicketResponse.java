package com.support.ticket.dto;

import com.support.ticket.model.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TicketResponse {

    private UUID id;
    private String customerId;
    private String customerEmail;
    private String customerName;
    private String subject;
    private String description;
    private Category category;
    private Priority priority;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private String assignedTo;
    private List<String> tags;
    private MetadataResponse metadata;
    private Double classificationConfidence;

    public static TicketResponse fromEntity(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.id = ticket.getId();
        response.customerId = ticket.getCustomerId();
        response.customerEmail = ticket.getCustomerEmail();
        response.customerName = ticket.getCustomerName();
        response.subject = ticket.getSubject();
        response.description = ticket.getDescription();
        response.category = ticket.getCategory();
        response.priority = ticket.getPriority();
        response.status = ticket.getStatus();
        response.createdAt = ticket.getCreatedAt();
        response.updatedAt = ticket.getUpdatedAt();
        response.resolvedAt = ticket.getResolvedAt();
        response.assignedTo = ticket.getAssignedTo();
        response.tags = ticket.getTags();
        response.classificationConfidence = ticket.getClassificationConfidence();
        if (ticket.getMetadata() != null) {
            MetadataResponse meta = new MetadataResponse();
            meta.setSource(ticket.getMetadata().getSource());
            meta.setBrowser(ticket.getMetadata().getBrowser());
            meta.setDeviceType(ticket.getMetadata().getDeviceType());
            response.metadata = meta;
        }
        return response;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public MetadataResponse getMetadata() { return metadata; }
    public void setMetadata(MetadataResponse metadata) { this.metadata = metadata; }

    public Double getClassificationConfidence() { return classificationConfidence; }
    public void setClassificationConfidence(Double classificationConfidence) { this.classificationConfidence = classificationConfidence; }

    public static class MetadataResponse {
        private Source source;
        private String browser;
        private DeviceType deviceType;

        public Source getSource() { return source; }
        public void setSource(Source source) { this.source = source; }
        public String getBrowser() { return browser; }
        public void setBrowser(String browser) { this.browser = browser; }
        public DeviceType getDeviceType() { return deviceType; }
        public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }
    }
}
