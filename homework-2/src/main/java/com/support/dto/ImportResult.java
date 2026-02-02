package com.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResult {
    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    
    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImportError {
        private int lineNumber;
        private String recordData;
        private String errorMessage;
    }
}
