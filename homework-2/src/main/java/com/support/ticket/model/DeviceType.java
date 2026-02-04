package com.support.ticket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeviceType {
    DESKTOP("desktop"),
    MOBILE("mobile"),
    TABLET("tablet");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DeviceType fromValue(String value) {
        for (DeviceType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid device type: " + value);
    }
}
