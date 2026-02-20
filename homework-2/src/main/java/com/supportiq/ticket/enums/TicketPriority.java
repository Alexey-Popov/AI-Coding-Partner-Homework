package com.supportiq.ticket.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketPriority {
    URGENT("urgent"),
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    private final String value;

    TicketPriority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TicketPriority fromValue(String value) {
        for (TicketPriority priority : values()) {
            if (priority.value.equalsIgnoreCase(value) || priority.name().equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority: " + value);
    }
}
