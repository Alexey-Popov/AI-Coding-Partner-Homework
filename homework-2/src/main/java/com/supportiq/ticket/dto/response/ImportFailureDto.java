package com.supportiq.ticket.dto.response;

import java.util.List;

public class ImportFailureDto {

    private int recordIndex;
    private List<String> errors;

    public ImportFailureDto() {}

    public ImportFailureDto(int recordIndex, List<String> errors) {
        this.recordIndex = recordIndex;
        this.errors = errors;
    }

    public int getRecordIndex() { return recordIndex; }
    public void setRecordIndex(int recordIndex) { this.recordIndex = recordIndex; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
