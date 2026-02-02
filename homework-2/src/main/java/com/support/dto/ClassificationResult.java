package com.support.dto;

import com.support.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassificationResult {
    private Ticket.Category category;
    private Ticket.Priority priority;
    private double confidence;
    private String reasoning;
    private List<String> keywordsFound;
}
