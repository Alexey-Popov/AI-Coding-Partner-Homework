package com.support.ticket.service;

import com.support.ticket.dto.ClassificationResult;
import com.support.ticket.model.Category;
import com.support.ticket.model.Priority;
import com.support.ticket.model.Ticket;
import com.support.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ClassificationService {

    private final TicketRepository ticketRepository;

    private static final Map<Category, List<KeywordEntry>> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    private static final Map<Priority, List<String>> PRIORITY_KEYWORDS = new LinkedHashMap<>();
    private static final List<Pattern> BUG_REPORT_PATTERNS = new ArrayList<>();
    private static final double CONFIDENCE_THRESHOLD = 0.3;

    static {
        CATEGORY_KEYWORDS.put(Category.ACCOUNT_ACCESS, List.of(
                new KeywordEntry("login", 1.0), new KeywordEntry("password", 1.0),
                new KeywordEntry("2fa", 1.5), new KeywordEntry("locked out", 1.5),
                new KeywordEntry("sign in", 1.0), new KeywordEntry("authentication", 1.2),
                new KeywordEntry("reset password", 1.5), new KeywordEntry("can't log in", 1.5),
                new KeywordEntry("access denied", 1.2), new KeywordEntry("account locked", 1.5)
        ));

        CATEGORY_KEYWORDS.put(Category.TECHNICAL_ISSUE, List.of(
                new KeywordEntry("error", 1.0), new KeywordEntry("crash", 1.2),
                new KeywordEntry("broken", 1.0), new KeywordEntry("not working", 1.2),
                new KeywordEntry("exception", 1.0), new KeywordEntry("slow", 0.8),
                new KeywordEntry("freeze", 1.0), new KeywordEntry("unresponsive", 1.0),
                new KeywordEntry("500", 1.0), new KeywordEntry("timeout", 1.0)
        ));

        CATEGORY_KEYWORDS.put(Category.BILLING_QUESTION, List.of(
                new KeywordEntry("payment", 1.2), new KeywordEntry("invoice", 1.2),
                new KeywordEntry("refund", 1.5), new KeywordEntry("charge", 1.0),
                new KeywordEntry("subscription", 1.0), new KeywordEntry("billing", 1.5),
                new KeywordEntry("receipt", 1.0), new KeywordEntry("pricing", 1.0),
                new KeywordEntry("plan", 0.5), new KeywordEntry("upgrade", 0.8)
        ));

        CATEGORY_KEYWORDS.put(Category.FEATURE_REQUEST, List.of(
                new KeywordEntry("feature", 1.2), new KeywordEntry("suggestion", 1.0),
                new KeywordEntry("enhance", 1.0), new KeywordEntry("would be nice", 1.5),
                new KeywordEntry("add support", 1.2), new KeywordEntry("request", 0.5),
                new KeywordEntry("wish", 0.8), new KeywordEntry("improve", 0.8),
                new KeywordEntry("could you add", 1.5)
        ));

        CATEGORY_KEYWORDS.put(Category.BUG_REPORT, List.of(
                new KeywordEntry("steps to reproduce", 3.0), new KeywordEntry("expected", 1.0),
                new KeywordEntry("actual", 1.0), new KeywordEntry("reproduce", 2.0),
                new KeywordEntry("defect", 2.0), new KeywordEntry("regression", 2.5),
                new KeywordEntry("str:", 3.0)
        ));

        BUG_REPORT_PATTERNS.add(Pattern.compile("\\d+\\.\\s", Pattern.MULTILINE));
        BUG_REPORT_PATTERNS.add(Pattern.compile("(?i)steps\\s*to\\s*reproduce"));
        BUG_REPORT_PATTERNS.add(Pattern.compile("(?i)expected\\s*(result|behavior|outcome)"));
        BUG_REPORT_PATTERNS.add(Pattern.compile("(?i)actual\\s*(result|behavior|outcome)"));

        PRIORITY_KEYWORDS.put(Priority.URGENT, List.of(
                "can't access", "critical", "production down", "security",
                "data loss", "outage", "emergency"
        ));

        PRIORITY_KEYWORDS.put(Priority.HIGH, List.of(
                "important", "blocking", "asap", "need immediately"
        ));

        PRIORITY_KEYWORDS.put(Priority.LOW, List.of(
                "minor", "cosmetic", "suggestion", "nice to have", "when you get a chance"
        ));
    }

    public ClassificationService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public ClassificationResult classify(Ticket ticket) {
        String text = (ticket.getSubject() + " " + ticket.getDescription()).toLowerCase();

        Map<Category, Double> scores = new LinkedHashMap<>();
        Map<Category, List<String>> foundKeywords = new LinkedHashMap<>();

        for (Map.Entry<Category, List<KeywordEntry>> entry : CATEGORY_KEYWORDS.entrySet()) {
            Category category = entry.getKey();
            double score = 0;
            List<String> keywords = new ArrayList<>();
            for (KeywordEntry keyword : entry.getValue()) {
                if (text.contains(keyword.word.toLowerCase())) {
                    score += keyword.weight;
                    keywords.add(keyword.word);
                }
            }
            scores.put(category, score);
            foundKeywords.put(category, keywords);
        }

        boolean hasStructuralPatterns = false;
        for (Pattern pattern : BUG_REPORT_PATTERNS) {
            if (pattern.matcher(text).find()) {
                hasStructuralPatterns = true;
                scores.merge(Category.BUG_REPORT, 2.0, Double::sum);
                break;
            }
        }

        // Disambiguation
        double bugScore = scores.getOrDefault(Category.BUG_REPORT, 0.0);
        double techScore = scores.getOrDefault(Category.TECHNICAL_ISSUE, 0.0);
        double featureScore = scores.getOrDefault(Category.FEATURE_REQUEST, 0.0);
        double billingScore = scores.getOrDefault(Category.BILLING_QUESTION, 0.0);

        if (bugScore > 0 && techScore > 0) {
            if (hasStructuralPatterns) {
                scores.put(Category.TECHNICAL_ISSUE, techScore * 0.5);
            } else {
                scores.put(Category.BUG_REPORT, bugScore * 0.5);
            }
        }

        if (bugScore > 0 && featureScore > 0) {
            if (foundKeywords.getOrDefault(Category.BUG_REPORT, List.of()).stream()
                    .anyMatch(k -> k.equals("defect") || k.equals("regression"))) {
                scores.put(Category.FEATURE_REQUEST, featureScore * 0.5);
            }
        }

        if (featureScore > 0 && billingScore > 0) {
            int featureHits = foundKeywords.getOrDefault(Category.FEATURE_REQUEST, List.of()).size();
            int billingHits = foundKeywords.getOrDefault(Category.BILLING_QUESTION, List.of()).size();
            if (billingHits > featureHits) {
                scores.put(Category.FEATURE_REQUEST, featureScore * 0.5);
            } else if (featureHits > billingHits) {
                scores.put(Category.BILLING_QUESTION, billingScore * 0.5);
            }
        }

        // Find best category
        Category bestCategory = Category.OTHER;
        double bestScore = 0;
        double secondBestScore = 0;
        List<String> bestKeywords = new ArrayList<>();

        for (Map.Entry<Category, Double> entry : scores.entrySet()) {
            if (entry.getValue() > bestScore) {
                secondBestScore = bestScore;
                bestScore = entry.getValue();
                bestCategory = entry.getKey();
                bestKeywords = foundKeywords.getOrDefault(entry.getKey(), List.of());
            } else if (entry.getValue() > secondBestScore) {
                secondBestScore = entry.getValue();
            }
        }

        // Calculate confidence (0.0 - 1.0)
        double totalWeight = CATEGORY_KEYWORDS.values().stream()
                .flatMap(List::stream)
                .mapToDouble(k -> k.weight)
                .max().orElse(1.0) * 5;

        double confidence = Math.min(1.0, bestScore / totalWeight);

        if (secondBestScore > 0 && bestScore > 0) {
            double ratio = secondBestScore / bestScore;
            if (ratio > 0.7) {
                confidence *= (1.0 - (ratio - 0.7));
            }
        }

        if (bestScore == 0 || confidence < CONFIDENCE_THRESHOLD) {
            bestCategory = Category.OTHER;
            confidence = bestScore > 0 ? confidence : 0.0;
        }

        // Detect priority
        Priority priority = detectPriority(text);
        List<String> allKeywords = new ArrayList<>(bestKeywords);

        // Build reasoning
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Classified as ").append(bestCategory.getValue());
        if (!bestKeywords.isEmpty()) {
            reasoning.append(" based on keywords: ").append(String.join(", ", bestKeywords));
        }
        if (hasStructuralPatterns) {
            reasoning.append(". Structural patterns detected (reproduction steps).");
        }
        reasoning.append(". Priority set to ").append(priority.getValue()).append(".");

        return new ClassificationResult(bestCategory, priority, confidence,
                reasoning.toString(), allKeywords);
    }

    public ClassificationResult classifyAndUpdate(Ticket ticket) {
        ClassificationResult result = classify(ticket);
        ticket.setCategory(result.getCategory());
        ticket.setPriority(result.getPriority());
        ticket.setClassificationConfidence(result.getConfidence());
        ticketRepository.save(ticket);
        return result;
    }

    private Priority detectPriority(String text) {
        for (Map.Entry<Priority, List<String>> entry : PRIORITY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (text.contains(keyword.toLowerCase())) {
                    return entry.getKey();
                }
            }
        }
        return Priority.MEDIUM;
    }

    private static class KeywordEntry {
        final String word;
        final double weight;

        KeywordEntry(String word, double weight) {
            this.word = word;
            this.weight = weight;
        }
    }
}
