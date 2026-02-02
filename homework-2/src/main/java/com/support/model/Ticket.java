package com.support.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    
    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;
    
    @NotBlank(message = "Customer ID is required")
    @Column(nullable = false)
    private String customerId;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false)
    private String customerEmail;
    
    @NotBlank(message = "Customer name is required")
    @Column(nullable = false)
    private String customerName;
    
    @NotBlank(message = "Subject is required")
    @Size(min = 1, max = 200, message = "Subject must be 1-200 characters")
    @Column(nullable = false, length = 200)
    private String subject;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be 10-2000 characters")
    @Column(nullable = false, length = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.NEW;
    
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime resolvedAt;
    
    private String assignedTo;
    
    @ElementCollection
    @CollectionTable(name = "ticket_tags", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "tag")
    private List<String> tags;
    
    @Embedded
    private Metadata metadata;
    
    @Transient
    private Double classificationConfidence;
    
    @PrePersist
    public void prePersist() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        if (category == null) {
            category = Category.OTHER;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
    }
    
    public enum Category {
        ACCOUNT_ACCESS,
        TECHNICAL_ISSUE,
        BILLING_QUESTION,
        FEATURE_REQUEST,
        BUG_REPORT,
        OTHER
    }
    
    public enum Priority {
        URGENT,
        HIGH,
        MEDIUM,
        LOW
    }
    
    public enum Status {
        NEW,
        IN_PROGRESS,
        WAITING_CUSTOMER,
        RESOLVED,
        CLOSED
    }
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Metadata {
        @Enumerated(EnumType.STRING)
        private Source source;
        
        private String browser;
        
        @Enumerated(EnumType.STRING)
        private DeviceType deviceType;
        
        public enum Source {
            WEB_FORM,
            EMAIL,
            API,
            CHAT,
            PHONE
        }
        
        public enum DeviceType {
            DESKTOP,
            MOBILE,
            TABLET
        }
    }
}
