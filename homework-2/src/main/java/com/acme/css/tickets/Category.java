package com.acme.css.tickets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    ACCOUNT_ACCESS,
    TECHNICAL_ISSUE,
    BILLING_QUESTION,
    FEATURE_REQUEST,
    BUG_REPORT,
    OTHER;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Category forValue(String value) {
        if (value == null) return null;
        return Category.valueOf(value.trim().toUpperCase());
    }
}
