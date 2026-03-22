package com.support.ticket.dto;

import com.support.ticket.model.DeviceType;
import com.support.ticket.model.Source;

public class MetadataRequest {

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
