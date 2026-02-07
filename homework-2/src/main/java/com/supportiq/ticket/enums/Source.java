package com.supportiq.ticket.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Source {
    WEB_FORM("web_form"),
    EMAIL("email"),
    API("api"),
    CHAT("chat"),
    PHONE("phone");

    private final String value;

    Source(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Source fromValue(String value) {
        for (Source source : values()) {
            if (source.value.equalsIgnoreCase(value) || source.name().equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown source: " + value);
    }
}
