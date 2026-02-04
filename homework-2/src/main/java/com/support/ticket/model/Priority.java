package com.support.ticket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    URGENT("urgent"),
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    private final String value;

    Priority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Priority fromValue(String value) {
        for (Priority priority : values()) {
            if (priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid priority: " + value);
    }
}
