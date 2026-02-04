package com.support.ticket.dto;

import com.support.ticket.model.Category;
import com.support.ticket.model.Priority;
import com.support.ticket.model.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UpdateTicketRequest {

    @Email(message = "Invalid email format")
    private String customerEmail;

    private String customerName;

    @Size(min = 1, max = 200, message = "Subject must be between 1 and 200 characters")
    private String subject;

    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    private Category category;
    private Priority priority;
    private Status status;
    private String assignedTo;
    private List<String> tags;

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

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
