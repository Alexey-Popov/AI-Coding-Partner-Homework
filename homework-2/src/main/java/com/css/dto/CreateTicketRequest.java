package com.css.dto;

import com.css.model.DeviceType;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import com.css.model.TicketSource;

import java.util.List;

public class CreateTicketRequest {
    private String customerId;
    private String customerEmail;
    private String customerName;
    private String subject;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private List<String> tags;
    private MetadataRequest metadata;
    private Boolean autoClassify;

    public static class MetadataRequest {
        private TicketSource source;
        private String browser;
        private DeviceType deviceType;

        public TicketSource getSource() {
            return source;
        }

        public void setSource(TicketSource source) {
            this.source = source;
        }

        public String getBrowser() {
            return browser;
        }

        public void setBrowser(String browser) {
            this.browser = browser;
        }

        public DeviceType getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(DeviceType deviceType) {
            this.deviceType = deviceType;
        }
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public MetadataRequest getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataRequest metadata) {
        this.metadata = metadata;
    }

    public Boolean getAutoClassify() {
        return autoClassify;
    }

    public void setAutoClassify(Boolean autoClassify) {
        this.autoClassify = autoClassify;
    }
}

