package com.css.dto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    @Test
    void constructor_setsStatus() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Validation failed");
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void constructor_setsError() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Validation failed");
        assertThat(response.getError()).isEqualTo("Bad Request");
    }

    @Test
    void constructor_setsMessage() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Validation failed");
        assertThat(response.getMessage()).isEqualTo("Validation failed");
    }

    @Test
    void constructor_setsTimestampToCurrentTime() {
        long before = System.currentTimeMillis();
        ErrorResponse response = new ErrorResponse(500, "Internal Server Error", "Unexpected error");
        long after = System.currentTimeMillis();
        assertThat(response.getTimestamp()).isBetween(before, after);
    }

    @Test
    void constructor_initializesEmptyFieldErrors() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Validation failed");
        assertThat(response.getFieldErrors()).isNotNull().isEmpty();
    }

    // -------------------------------------------------------------------------
    // Setters / getters
    // -------------------------------------------------------------------------

    @Test
    void setStatus_roundTrip() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "msg");
        response.setStatus(404);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void setError_roundTrip() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "msg");
        response.setError("Not Found");
        assertThat(response.getError()).isEqualTo("Not Found");
    }

    @Test
    void setMessage_roundTrip() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "msg");
        response.setMessage("Updated message");
        assertThat(response.getMessage()).isEqualTo("Updated message");
    }

    @Test
    void setTimestamp_roundTrip() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "msg");
        response.setTimestamp(123456789L);
        assertThat(response.getTimestamp()).isEqualTo(123456789L);
    }

    @Test
    void setFieldErrors_roundTrip() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "msg");
        Map<String, String> errors = new HashMap<>();
        errors.put("email", "must not be blank");
        response.setFieldErrors(errors);
        assertThat(response.getFieldErrors()).containsEntry("email", "must not be blank");
    }

    // -------------------------------------------------------------------------
    // addFieldError
    // -------------------------------------------------------------------------

    @Test
    void addFieldError_addsEntry() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "msg");
        response.addFieldError("subject", "must not be empty");
        assertThat(response.getFieldErrors()).containsEntry("subject", "must not be empty");
    }

    @Test
    void addFieldError_multipleEntries() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "msg");
        response.addFieldError("email", "invalid format");
        response.addFieldError("subject", "must not be empty");
        assertThat(response.getFieldErrors())
                .hasSize(2)
                .containsEntry("email", "invalid format")
                .containsEntry("subject", "must not be empty");
    }

    @Test
    void addFieldError_overwritesExistingKey() {
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "msg");
        response.addFieldError("email", "first message");
        response.addFieldError("email", "second message");
        assertThat(response.getFieldErrors())
                .hasSize(1)
                .containsEntry("email", "second message");
    }

    // -------------------------------------------------------------------------
    // Different status codes
    // -------------------------------------------------------------------------

    @Test
    void constructor_withStatus404() {
        ErrorResponse response = new ErrorResponse(404, "Not Found", "Resource missing");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getError()).isEqualTo("Not Found");
        assertThat(response.getMessage()).isEqualTo("Resource missing");
    }

    @Test
    void constructor_withStatus500() {
        ErrorResponse response = new ErrorResponse(500, "Internal Server Error", "Unexpected");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getError()).isEqualTo("Internal Server Error");
    }
}
