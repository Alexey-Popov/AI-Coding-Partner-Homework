package com.acme.css.tickets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    URGENT,
    HIGH,
    MEDIUM,
    LOW;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Priority forValue(String value) {
        if (value == null) return null;
        return Priority.valueOf(value.trim().toUpperCase());
    }
}
