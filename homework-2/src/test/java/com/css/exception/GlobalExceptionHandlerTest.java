package com.css.exception;

import com.css.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ── TicketNotFoundException → 404 Not Found ──────────────────────────────

    @Test
    void handleTicketNotFound_returns404() {
        TicketNotFoundException ex = new TicketNotFoundException("Ticket abc not found");
        ResponseEntity<ErrorResponse> response = handler.handleTicketNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("Ticket abc not found");
    }

    @Test
    void handleTicketNotFound_bodyHasEmptyFieldErrors() {
        ResponseEntity<ErrorResponse> response =
                handler.handleTicketNotFound(new TicketNotFoundException("not found"));
        assertThat(response.getBody().getFieldErrors()).isEmpty();
    }

    // ── ValidationException → 400 Bad Request ───────────────────────────────

    @Test
    void handleValidation_returns400() {
        Map<String, String> fieldErrors = Map.of("title", "must not be blank");
        ValidationException ex = new ValidationException("Validation failed", fieldErrors);
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
    }

    @Test
    void handleValidation_propagatesFieldErrors() {
        Map<String, String> fieldErrors = Map.of("priority", "invalid value", "status", "unknown");
        ValidationException ex = new ValidationException("bad input", fieldErrors);
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getBody().getFieldErrors())
                .containsEntry("priority", "invalid value")
                .containsEntry("status", "unknown");
    }

    @Test
    void handleValidation_emptyFieldErrors() {
        ValidationException ex = new ValidationException("Validation failed", Map.of());
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);
        assertThat(response.getBody().getFieldErrors()).isEmpty();
    }

    // ── ImportException → 400 Import Error ──────────────────────────────────

    @Test
    void handleImportError_returns400() {
        ImportException ex = new ImportException("Malformed CSV on line 5");
        ResponseEntity<ErrorResponse> response = handler.handleImportError(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Import Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Malformed CSV on line 5");
    }

    @Test
    void handleImportError_bodyHasEmptyFieldErrors() {
        ResponseEntity<ErrorResponse> response =
                handler.handleImportError(new ImportException("bad import"));
        assertThat(response.getBody().getFieldErrors()).isEmpty();
    }

    // ── IllegalArgumentException → 400 Bad Request ──────────────────────────

    @Test
    void handleIllegalArgument_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Unknown enum value: XYZ");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Unknown enum value: XYZ");
    }

    @Test
    void handleIllegalArgument_bodyHasEmptyFieldErrors() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad arg"));
        assertThat(response.getBody().getFieldErrors()).isEmpty();
    }

    // ── Generic Exception → 500 Internal Server Error ───────────────────────

    @Test
    void handleGeneral_returns500() {
        Exception ex = new RuntimeException("something went wrong");
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage())
                .contains("An unexpected error occurred")
                .contains("something went wrong");
    }

    @Test
    void handleGeneral_messageContainsExceptionText() {
        Exception ex = new Exception("db connection refused");
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);
        assertThat(response.getBody().getMessage()).contains("db connection refused");
    }

    // ── ErrorResponse timestamp is populated ────────────────────────────────

    @Test
    void responseBody_hasPositiveTimestamp() {
        long before = System.currentTimeMillis();
        ResponseEntity<ErrorResponse> response =
                handler.handleTicketNotFound(new TicketNotFoundException("x"));
        long after = System.currentTimeMillis();

        assertThat(response.getBody().getTimestamp())
                .isGreaterThanOrEqualTo(before)
                .isLessThanOrEqualTo(after);
    }
}
