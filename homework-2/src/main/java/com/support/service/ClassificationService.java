package com.support.service;

import com.support.dto.ClassificationResult;
import com.support.model.Ticket;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ClassificationService {
    
    private static final Map<String, Ticket.Category> CATEGORY_KEYWORDS = new LinkedHashMap<>() {{
        put("reproduce|steps to reproduce|defect|broken|bug found", Ticket.Category.BUG_REPORT);
        put("login|password|2fa|authentication|sign in|access denied", Ticket.Category.ACCOUNT_ACCESS);
        put("payment|invoice|refund|billing|charge|subscription", Ticket.Category.BILLING_QUESTION);
        put("enhancement|suggestion|improvement|feature|could you add", Ticket.Category.FEATURE_REQUEST);
        put("bug|error|crash|exception|stacktrace|failure", Ticket.Category.TECHNICAL_ISSUE);
    }};
    
    private static final Map<String, Ticket.Priority> PRIORITY_KEYWORDS = Map.of(
        "can't access|critical|production down|security|urgent|emergency", Ticket.Priority.URGENT,
        "important|blocking|asap|high priority|need urgent", Ticket.Priority.HIGH,
        "minor|cosmetic|suggestion|nice to have|low priority", Ticket.Priority.LOW
    );
    
    public ClassificationResult classifyTicket(String subject, String description) {
        String combinedText = (subject + " " + description).toLowerCase();
        
        Ticket.Category category = detectCategory(combinedText);
        Ticket.Priority priority = detectPriority(combinedText);
        List<String> keywordsFound = findKeywords(combinedText);
        double confidence = calculateConfidence(keywordsFound);
        String reasoning = generateReasoning(category, priority, keywordsFound);
        
        return ClassificationResult.builder()
            .category(category)
            .priority(priority)
            .confidence(confidence)
            .reasoning(reasoning)
            .keywordsFound(keywordsFound)
            .build();
    }
    
    private Ticket.Category detectCategory(String text) {
        for (Map.Entry<String, Ticket.Category> entry : CATEGORY_KEYWORDS.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(text).find()) {
                return entry.getValue();
            }
        }
        return Ticket.Category.OTHER;
    }
    
    private Ticket.Priority detectPriority(String text) {
        for (Map.Entry<String, Ticket.Priority> entry : PRIORITY_KEYWORDS.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(text).find()) {
                return entry.getValue();
            }
        }
        return Ticket.Priority.MEDIUM;
    }
    
    private List<String> findKeywords(String text) {
        List<String> found = new ArrayList<>();
        
        CATEGORY_KEYWORDS.forEach((keywords, category) -> {
            Arrays.stream(keywords.split("\\|"))
                .filter(keyword -> text.contains(keyword.toLowerCase()))
                .forEach(found::add);
        });
        
        PRIORITY_KEYWORDS.forEach((keywords, priority) -> {
            Arrays.stream(keywords.split("\\|"))
                .filter(keyword -> text.contains(keyword.toLowerCase()))
                .forEach(found::add);
        });
        
        return found;
    }
    
    private double calculateConfidence(List<String> keywords) {
        if (keywords.isEmpty()) return 0.3;
        if (keywords.size() == 1) return 0.6;
        if (keywords.size() == 2) return 0.8;
        return 0.95;
    }
    
    private String generateReasoning(Ticket.Category category, Ticket.Priority priority, List<String> keywords) {
        if (keywords.isEmpty()) {
            return "No specific keywords detected. Defaulting to OTHER/MEDIUM.";
        }
        return String.format("Classified as %s/%s based on keywords: %s", 
            category, priority, String.join(", ", keywords));
    }
}
