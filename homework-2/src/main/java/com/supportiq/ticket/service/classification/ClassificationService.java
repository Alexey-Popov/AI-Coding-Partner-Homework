package com.supportiq.ticket.service.classification;

import com.supportiq.ticket.enums.TicketCategory;
import com.supportiq.ticket.enums.TicketPriority;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClassificationService {

    private static final Map<TicketCategory, List<String>> CATEGORY_KEYWORDS = Map.of(
            TicketCategory.ACCOUNT_ACCESS, List.of(
                    "login", "password", "reset", "locked", "account", "access", "sign in", "sign-in",
                    "authentication", "credentials", "two-factor", "2fa", "mfa", "logout", "session"
            ),
            TicketCategory.TECHNICAL_ISSUE, List.of(
                    "error", "crash", "bug", "not working", "broken", "slow", "timeout", "freeze",
                    "exception", "failure", "down", "outage", "unresponsive", "load", "performance"
            ),
            TicketCategory.BILLING_QUESTION, List.of(
                    "billing", "invoice", "charge", "payment", "subscription", "refund", "price",
                    "cost", "plan", "upgrade", "downgrade", "cancel", "receipt", "credit"
            ),
            TicketCategory.FEATURE_REQUEST, List.of(
                    "feature", "request", "suggestion", "improve", "enhancement", "add", "wish",
                    "would like", "could you", "new functionality", "integration", "support for"
            ),
            TicketCategory.BUG_REPORT, List.of(
                    "bug", "defect", "issue", "unexpected", "incorrect", "wrong", "doesn't work",
                    "not displaying", "missing", "broken", "glitch", "regression", "reproduce"
            )
    );

    private static final Map<TicketPriority, List<String>> PRIORITY_KEYWORDS;

    static {
        // LinkedHashMap preserves insertion order: URGENT checked first, then HIGH, then LOW
        Map<TicketPriority, List<String>> map = new LinkedHashMap<>();
        map.put(TicketPriority.URGENT, List.of(
                "urgent", "emergency", "critical", "asap", "immediately", "production down",
                "data loss", "security breach", "blocked", "cannot work"
        ));
        map.put(TicketPriority.HIGH, List.of(
                "important", "high priority", "serious", "major", "affecting many",
                "widespread", "revenue", "deadline", "escalate"
        ));
        map.put(TicketPriority.LOW, List.of(
                "minor", "cosmetic", "low priority", "nice to have", "when possible",
                "no rush", "eventually", "suggestion"
        ));
        PRIORITY_KEYWORDS = Collections.unmodifiableMap(map);
    }

    public ClassificationResult classify(String subject, String description) {
        String text = (subject + " " + description).toLowerCase();

        Map<TicketCategory, List<String>> categoryMatches = new LinkedHashMap<>();
        for (var entry : CATEGORY_KEYWORDS.entrySet()) {
            List<String> matched = entry.getValue().stream()
                    .filter(text::contains)
                    .toList();
            if (!matched.isEmpty()) {
                categoryMatches.put(entry.getKey(), matched);
            }
        }

        TicketCategory bestCategory = TicketCategory.OTHER;
        List<String> bestCategoryKeywords = List.of();
        int maxMatches = 0;

        for (var entry : categoryMatches.entrySet()) {
            if (entry.getValue().size() > maxMatches) {
                maxMatches = entry.getValue().size();
                bestCategory = entry.getKey();
                bestCategoryKeywords = entry.getValue();
            }
        }

        TicketPriority detectedPriority = TicketPriority.MEDIUM;
        List<String> priorityKeywords = new ArrayList<>();

        for (var entry : PRIORITY_KEYWORDS.entrySet()) {
            List<String> matched = entry.getValue().stream()
                    .filter(text::contains)
                    .toList();
            if (!matched.isEmpty()) {
                detectedPriority = entry.getKey();
                priorityKeywords.addAll(matched);
                break;
            }
        }

        List<String> allKeywords = new ArrayList<>(bestCategoryKeywords);
        allKeywords.addAll(priorityKeywords);

        double confidence = Math.min(1.0, 0.3 + (allKeywords.size() * 0.1));
        if (bestCategory == TicketCategory.OTHER) {
            confidence = 0.3;
        }

        String reasoning = buildReasoning(bestCategory, detectedPriority, bestCategoryKeywords, priorityKeywords);

        return new ClassificationResult(bestCategory, detectedPriority, confidence, reasoning, allKeywords);
    }

    private String buildReasoning(TicketCategory category, TicketPriority priority,
                                  List<String> categoryKeywords, List<String> priorityKeywords) {
        StringBuilder sb = new StringBuilder();

        if (category == TicketCategory.OTHER) {
            sb.append("No strong keyword matches found. Classified as OTHER with low confidence.");
        } else {
            sb.append("Classified as ").append(category.getValue())
                    .append(" based on keywords: ").append(String.join(", ", categoryKeywords)).append(".");
        }

        if (!priorityKeywords.isEmpty()) {
            sb.append(" Priority set to ").append(priority.getValue())
                    .append(" based on keywords: ").append(String.join(", ", priorityKeywords)).append(".");
        } else {
            sb.append(" Default priority: ").append(priority.getValue()).append(".");
        }

        return sb.toString();
    }
}
