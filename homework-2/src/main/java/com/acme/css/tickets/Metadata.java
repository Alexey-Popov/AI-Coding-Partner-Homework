package com.acme.css.tickets;

import jakarta.validation.constraints.NotBlank;

public class Metadata {

    private String source; // web_form | email | api | chat | phone
    private String browser;
    private String device_type; // desktop | mobile | tablet

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }
}
