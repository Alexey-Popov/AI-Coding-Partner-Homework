package com.css.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketSource {
    WEB_FORM("web_form"),
    EMAIL("email"),
    API("api"),
    CHAT("chat"),
    PHONE("phone");

    private final String value;

    TicketSource(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TicketSource fromValue(String value) {
        for (TicketSource source : TicketSource.values()) {
            if (source.value.equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown TicketSource: " + value);
    }
}

