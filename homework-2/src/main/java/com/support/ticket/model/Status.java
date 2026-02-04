package com.support.ticket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    NEW("new"),
    IN_PROGRESS("in_progress"),
    WAITING_CUSTOMER("waiting_customer"),
    RESOLVED("resolved"),
    CLOSED("closed");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Status fromValue(String value) {
        for (Status status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + value);
    }
}
