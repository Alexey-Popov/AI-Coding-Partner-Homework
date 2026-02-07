package com.supportiq.ticket.dto.response;

import com.supportiq.ticket.enums.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TicketDto {

    private UUID id;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String subject;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketStatus status;
    private Source source;
    private String browser;
    private DeviceType deviceType;
    private Set<String> tags;
    private Double classificationConfidence;
    private String classificationReasoning;
    private List<String> classificationKeywords;
    private Instant lastClassifiedAt;
    private Instant resolvedAt;
    private String assignedTo;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public DeviceType getDeviceType() { return deviceType; }
    public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public Double getClassificationConfidence() { return classificationConfidence; }
    public void setClassificationConfidence(Double classificationConfidence) { this.classificationConfidence = classificationConfidence; }

    public String getClassificationReasoning() { return classificationReasoning; }
    public void setClassificationReasoning(String classificationReasoning) { this.classificationReasoning = classificationReasoning; }

    public List<String> getClassificationKeywords() { return classificationKeywords; }
    public void setClassificationKeywords(List<String> classificationKeywords) { this.classificationKeywords = classificationKeywords; }

    public Instant getLastClassifiedAt() { return lastClassifiedAt; }
    public void setLastClassifiedAt(Instant lastClassifiedAt) { this.lastClassifiedAt = lastClassifiedAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
