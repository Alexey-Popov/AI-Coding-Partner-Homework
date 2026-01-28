package com.example.demo.service;

import java.util.List;
import java.util.Map;

public class ValidationException extends RuntimeException {
    private final List<Map<String, String>> details;

    public ValidationException(List<Map<String, String>> details) {
        super("Validation failed");
        this.details = details;
    }

    public List<Map<String, String>> getDetails() {
        return details;
    }
}
