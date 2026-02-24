package com.acme.css.tickets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    NEW,
    IN_PROGRESS,
    WAITING_CUSTOMER,
    RESOLVED,
    CLOSED;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Status forValue(String value) {
        if (value == null) return null;
        return Status.valueOf(value.trim().toUpperCase());
    }
}
