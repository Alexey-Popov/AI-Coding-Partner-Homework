package com.css.model;

public class TicketMetadata {
    private TicketSource source;
    private String browser;
    private DeviceType deviceType;

    public TicketMetadata() {
    }

    public TicketMetadata(TicketSource source, String browser, DeviceType deviceType) {
        this.source = source;
        this.browser = browser;
        this.deviceType = deviceType;
    }

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

