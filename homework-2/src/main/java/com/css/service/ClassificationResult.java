package com.css.service;

import com.css.model.TicketCategory;
import com.css.model.TicketPriority;

import java.util.List;

public class ClassificationResult {
    private TicketCategory category;
    private TicketPriority priority;
    private double confidence;
    private String reasoning;
    private List<String> keywords;

    public ClassificationResult() {
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
