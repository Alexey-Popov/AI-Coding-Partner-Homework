package com.css.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    private List<ImportError> errors;
    private List<String> importedTicketIds;

    public ImportResult() {
        this.errors = new ArrayList<>();
        this.importedTicketIds = new ArrayList<>();
    }

    public static class ImportError {
        private int recordNumber;
        private String field;
        private String message;
        private String rawData;

        public ImportError(int recordNumber, String field, String message, String rawData) {
            this.recordNumber = recordNumber;
            this.field = field;
            this.message = message;
            this.rawData = rawData;
        }

        public int getRecordNumber() {
            return recordNumber;
        }

        public void setRecordNumber(int recordNumber) {
            this.recordNumber = recordNumber;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getRawData() {
            return rawData;
        }

        public void setRawData(String rawData) {
            this.rawData = rawData;
        }
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getSuccessfulRecords() {
        return successfulRecords;
    }

    public void setSuccessfulRecords(int successfulRecords) {
        this.successfulRecords = successfulRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(int failedRecords) {
        this.failedRecords = failedRecords;
    }

    public List<ImportError> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportError> errors) {
        this.errors = errors;
    }

    public List<String> getImportedTicketIds() {
        return importedTicketIds;
    }

    public void setImportedTicketIds(List<String> importedTicketIds) {
        this.importedTicketIds = importedTicketIds;
    }

    public void addError(ImportError error) {
        this.errors.add(error);
    }

    public void addImportedTicketId(String ticketId) {
        this.importedTicketIds.add(ticketId);
    }
}

