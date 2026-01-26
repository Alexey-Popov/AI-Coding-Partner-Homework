package com.banking.transactions.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidationErrorResponse {
    
    private String error;
    private List<ValidationDetail> details;

    public ValidationErrorResponse() {
        this.error = "Validation failed";
        this.details = new ArrayList<>();
    }

    public ValidationErrorResponse(String error, List<ValidationDetail> details) {
        this.error = error;
        this.details = details;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<ValidationDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ValidationDetail> details) {
        this.details = details;
    }

    public void addDetail(String field, String message) {
        this.details.add(new ValidationDetail(field, message));
    }

    public static class ValidationDetail {
        private String field;
        private String message;

        public ValidationDetail() {
        }

        public ValidationDetail(String field, String message) {
            this.field = field;
            this.message = message;
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
    }
}
