package com.support.ticket.dto;

import com.support.ticket.model.Category;
import com.support.ticket.model.Priority;
import java.util.List;

public class ClassificationResult {

    private Category category;
    private Priority priority;
    private double confidence;
    private String reasoning;
    private List<String> keywordsFound;

    public ClassificationResult() {}

    public ClassificationResult(Category category, Priority priority, double confidence,
                                String reasoning, List<String> keywordsFound) {
        this.category = category;
        this.priority = priority;
        this.confidence = confidence;
        this.reasoning = reasoning;
        this.keywordsFound = keywordsFound;
    }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public List<String> getKeywordsFound() { return keywordsFound; }
    public void setKeywordsFound(List<String> keywordsFound) { this.keywordsFound = keywordsFound; }
}
