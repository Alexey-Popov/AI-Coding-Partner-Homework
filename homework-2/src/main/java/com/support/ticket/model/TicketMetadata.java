package com.support.ticket.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class TicketMetadata {

    @Enumerated(EnumType.STRING)
    private Source source;

    private String browser;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    public TicketMetadata() {}

    public TicketMetadata(Source source, String browser, DeviceType deviceType) {
        this.source = source;
        this.browser = browser;
        this.deviceType = deviceType;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
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
