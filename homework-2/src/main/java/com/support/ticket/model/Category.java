package com.support.ticket.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    ACCOUNT_ACCESS("account_access"),
    TECHNICAL_ISSUE("technical_issue"),
    BILLING_QUESTION("billing_question"),
    FEATURE_REQUEST("feature_request"),
    BUG_REPORT("bug_report"),
    OTHER("other");

    private final String value;

    Category(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Category fromValue(String value) {
        for (Category category : values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category: " + value);
    }
}
