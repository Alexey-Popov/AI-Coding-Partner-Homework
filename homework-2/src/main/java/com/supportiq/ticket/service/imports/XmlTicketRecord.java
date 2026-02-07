package com.supportiq.ticket.service.imports;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTicketRecord {

    @XmlElement(name = "customer_id")
    private String customerId;

    @XmlElement(name = "customer_name")
    private String customerName;

    @XmlElement(name = "customer_email")
    private String customerEmail;

    @XmlElement(name = "subject")
    private String subject;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "category")
    private String category;

    @XmlElement(name = "priority")
    private String priority;

    @XmlElement(name = "source")
    private String source;

    @XmlElement(name = "browser")
    private String browser;

    @XmlElement(name = "device_type")
    private String deviceType;

    @XmlElement(name = "tags")
    private String tags;

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

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
