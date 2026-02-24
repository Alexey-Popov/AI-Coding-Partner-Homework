package com.css.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionClassesTest {

    // ── TicketNotFoundException ──────────────────────────────────────────────

    @Test
    void ticketNotFoundException_storesMessage() {
        TicketNotFoundException ex = new TicketNotFoundException("Ticket 123 not found");
        assertThat(ex.getMessage()).isEqualTo("Ticket 123 not found");
    }

    @Test
    void ticketNotFoundException_isRuntimeException() {
        assertThat(new TicketNotFoundException("msg"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void ticketNotFoundException_thrownAndCaught() {
        assertThatThrownBy(() -> { throw new TicketNotFoundException("not found"); })
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessage("not found");
    }

    // ── ImportException ──────────────────────────────────────────────────────

    @Test
    void importException_storesMessage() {
        ImportException ex = new ImportException("parse failed");
        assertThat(ex.getMessage()).isEqualTo("parse failed");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void importException_storesMessageAndCause() {
        Throwable cause = new IllegalStateException("root cause");
        ImportException ex = new ImportException("import failed", cause);
        assertThat(ex.getMessage()).isEqualTo("import failed");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void importException_isRuntimeException() {
        assertThat(new ImportException("msg")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void importException_thrownAndCaught() {
        assertThatThrownBy(() -> { throw new ImportException("bad csv"); })
                .isInstanceOf(ImportException.class)
                .hasMessage("bad csv");
    }

    // ── ValidationException ──────────────────────────────────────────────────

    @Test
    void validationException_storesMessageAndFieldErrors() {
        Map<String, String> errors = Map.of("title", "must not be blank", "priority", "invalid value");
        ValidationException ex = new ValidationException("Validation failed", errors);

        assertThat(ex.getMessage()).isEqualTo("Validation failed");
        assertThat(ex.getFieldErrors()).containsEntry("title", "must not be blank");
        assertThat(ex.getFieldErrors()).containsEntry("priority", "invalid value");
    }

    @Test
    void validationException_isRuntimeException() {
        assertThat(new ValidationException("msg", Map.of()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void validationException_emptyFieldErrors() {
        ValidationException ex = new ValidationException("Validation failed", Map.of());
        assertThat(ex.getFieldErrors()).isEmpty();
    }

    @Test
    void validationException_thrownAndCaught() {
        Map<String, String> errors = Map.of("field", "error");
        assertThatThrownBy(() -> { throw new ValidationException("invalid", errors); })
                .isInstanceOf(ValidationException.class)
                .hasMessage("invalid");
    }
}
