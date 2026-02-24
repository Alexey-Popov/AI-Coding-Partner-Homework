package com.css.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImportResultTest {

    // -------------------------------------------------------------------------
    // Default constructor
    // -------------------------------------------------------------------------

    @Test
    void defaultConstructor_errorsIsEmptyList() {
        ImportResult result = new ImportResult();
        assertThat(result.getErrors()).isNotNull().isEmpty();
    }

    @Test
    void defaultConstructor_importedTicketIdsIsEmptyList() {
        ImportResult result = new ImportResult();
        assertThat(result.getImportedTicketIds()).isNotNull().isEmpty();
    }

    @Test
    void defaultConstructor_countersAreZero() {
        ImportResult result = new ImportResult();
        assertThat(result.getTotalRecords()).isZero();
        assertThat(result.getSuccessfulRecords()).isZero();
        assertThat(result.getFailedRecords()).isZero();
    }

    // -------------------------------------------------------------------------
    // Counter setters / getters
    // -------------------------------------------------------------------------

    @Test
    void setTotalRecords_roundTrip() {
        ImportResult result = new ImportResult();
        result.setTotalRecords(10);
        assertThat(result.getTotalRecords()).isEqualTo(10);
    }

    @Test
    void setSuccessfulRecords_roundTrip() {
        ImportResult result = new ImportResult();
        result.setSuccessfulRecords(8);
        assertThat(result.getSuccessfulRecords()).isEqualTo(8);
    }

    @Test
    void setFailedRecords_roundTrip() {
        ImportResult result = new ImportResult();
        result.setFailedRecords(2);
        assertThat(result.getFailedRecords()).isEqualTo(2);
    }

    // -------------------------------------------------------------------------
    // Collection setters / getters
    // -------------------------------------------------------------------------

    @Test
    void setErrors_roundTrip() {
        ImportResult result = new ImportResult();
        List<ImportResult.ImportError> errors = new ArrayList<>();
        errors.add(new ImportResult.ImportError(1, "subject", "must not be empty", "{}"));
        result.setErrors(errors);
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    void setImportedTicketIds_roundTrip() {
        ImportResult result = new ImportResult();
        List<String> ids = List.of("id-1", "id-2");
        result.setImportedTicketIds(ids);
        assertThat(result.getImportedTicketIds()).containsExactly("id-1", "id-2");
    }

    // -------------------------------------------------------------------------
    // addError
    // -------------------------------------------------------------------------

    @Test
    void addError_appendsToList() {
        ImportResult result = new ImportResult();
        ImportResult.ImportError error = new ImportResult.ImportError(1, "field", "msg", "raw");
        result.addError(error);
        assertThat(result.getErrors()).hasSize(1).contains(error);
    }

    @Test
    void addError_multipleErrors_preservesOrder() {
        ImportResult result = new ImportResult();
        ImportResult.ImportError e1 = new ImportResult.ImportError(1, "f1", "m1", "r1");
        ImportResult.ImportError e2 = new ImportResult.ImportError(2, "f2", "m2", "r2");
        result.addError(e1);
        result.addError(e2);
        assertThat(result.getErrors()).containsExactly(e1, e2);
    }

    // -------------------------------------------------------------------------
    // addImportedTicketId
    // -------------------------------------------------------------------------

    @Test
    void addImportedTicketId_appendsToList() {
        ImportResult result = new ImportResult();
        result.addImportedTicketId("ticket-abc");
        assertThat(result.getImportedTicketIds()).containsExactly("ticket-abc");
    }

    @Test
    void addImportedTicketId_multipleIds_preservesOrder() {
        ImportResult result = new ImportResult();
        result.addImportedTicketId("ticket-1");
        result.addImportedTicketId("ticket-2");
        result.addImportedTicketId("ticket-3");
        assertThat(result.getImportedTicketIds()).containsExactly("ticket-1", "ticket-2", "ticket-3");
    }

    // -------------------------------------------------------------------------
    // ImportError inner class
    // -------------------------------------------------------------------------

    @Test
    void importError_constructor_setsAllFields() {
        ImportResult.ImportError error = new ImportResult.ImportError(3, "email", "invalid format", "{email: bad}");
        assertThat(error.getRecordNumber()).isEqualTo(3);
        assertThat(error.getField()).isEqualTo("email");
        assertThat(error.getMessage()).isEqualTo("invalid format");
        assertThat(error.getRawData()).isEqualTo("{email: bad}");
    }

    @Test
    void importError_setRecordNumber_roundTrip() {
        ImportResult.ImportError error = new ImportResult.ImportError(1, "f", "m", "r");
        error.setRecordNumber(99);
        assertThat(error.getRecordNumber()).isEqualTo(99);
    }

    @Test
    void importError_setField_roundTrip() {
        ImportResult.ImportError error = new ImportResult.ImportError(1, "f", "m", "r");
        error.setField("subject");
        assertThat(error.getField()).isEqualTo("subject");
    }

    @Test
    void importError_setMessage_roundTrip() {
        ImportResult.ImportError error = new ImportResult.ImportError(1, "f", "m", "r");
        error.setMessage("must not be blank");
        assertThat(error.getMessage()).isEqualTo("must not be blank");
    }

    @Test
    void importError_setRawData_roundTrip() {
        ImportResult.ImportError error = new ImportResult.ImportError(1, "f", "m", "r");
        error.setRawData("{\"id\":\"123\"}");
        assertThat(error.getRawData()).isEqualTo("{\"id\":\"123\"}");
    }

    // -------------------------------------------------------------------------
    // Combined scenario
    // -------------------------------------------------------------------------

    @Test
    void fullImportResult_scenario() {
        ImportResult result = new ImportResult();
        result.setTotalRecords(5);
        result.setSuccessfulRecords(4);
        result.setFailedRecords(1);
        result.addImportedTicketId("t-1");
        result.addImportedTicketId("t-2");
        result.addImportedTicketId("t-3");
        result.addImportedTicketId("t-4");
        result.addError(new ImportResult.ImportError(5, "email", "invalid", "raw-line-5"));

        assertThat(result.getTotalRecords()).isEqualTo(5);
        assertThat(result.getSuccessfulRecords()).isEqualTo(4);
        assertThat(result.getFailedRecords()).isEqualTo(1);
        assertThat(result.getImportedTicketIds()).hasSize(4);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getRecordNumber()).isEqualTo(5);
    }
}
