package com.css.service;

import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketClassificationService {

    private static final Logger log = LoggerFactory.getLogger(TicketClassificationService.class);

    private final Map<TicketCategory, Set<String>> categoryKeywords = new HashMap<>();
    private final Set<String> urgentKeywords = new HashSet<>();
    private final Set<String> highKeywords = new HashSet<>();
    private final Set<String> lowKeywords = new HashSet<>();

    public TicketClassificationService() {
        categoryKeywords.put(TicketCategory.ACCOUNT_ACCESS, new HashSet<>(Arrays.asList(
                "login", "log in", "password", "2fa", "two-factor", "can't access", "can't login", "cannot login", "forgot password")));

        categoryKeywords.put(TicketCategory.TECHNICAL_ISSUE, new HashSet<>(Arrays.asList(
                "error", "exception", "crash", "not responding", "failed", "stack trace", "timeout", "slow")));

        categoryKeywords.put(TicketCategory.BILLING_QUESTION, new HashSet<>(Arrays.asList(
                "invoice", "payment", "refund", "charge", "billing", "receipt")));

        categoryKeywords.put(TicketCategory.FEATURE_REQUEST, new HashSet<>(Arrays.asList(
                "feature", "enhancement", "suggest", "would be great", "improvement")));

        categoryKeywords.put(TicketCategory.BUG_REPORT, new HashSet<>(Arrays.asList(
                "reproduce", "reproduction", "steps to reproduce", "bug", "defect")));

        urgentKeywords.addAll(Arrays.asList("can't access", "critical", "production down", "security", "cannot access", "cannot login"));
        highKeywords.addAll(Arrays.asList("important", "blocking", "asap", "urgent"));
        lowKeywords.addAll(Arrays.asList("minor", "cosmetic", "suggestion", "feature"));
    }

    public ClassificationResult classify(Ticket ticket) {
        String text = (ticket.getSubject() == null ? "" : ticket.getSubject()) + " " +
                (ticket.getDescription() == null ? "" : ticket.getDescription());
        String lc = text.toLowerCase(Locale.ROOT);

        List<String> foundKeywords = new ArrayList<>();
        Map<TicketCategory, Integer> matches = new EnumMap<>(TicketCategory.class);
        for (TicketCategory cat : categoryKeywords.keySet()) {
            int count = 0;
            for (String kw : categoryKeywords.get(cat)) {
                if (lc.contains(kw)) {
                    foundKeywords.add(kw);
                    count++;
                }
            }
            matches.put(cat, count);
        }

        // Choose category with max matches
        TicketCategory chosenCategory = TicketCategory.OTHER;
        int max = 0;
        for (Map.Entry<TicketCategory, Integer> e : matches.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                chosenCategory = e.getKey();
            }
        }

        // Priority rules
        TicketPriority chosenPriority = TicketPriority.MEDIUM;
        for (String kw : urgentKeywords) {
            if (lc.contains(kw)) {
                chosenPriority = TicketPriority.URGENT;
                break;
            }
        }
        if (chosenPriority != TicketPriority.URGENT) {
            for (String kw : highKeywords) {
                if (lc.contains(kw)) {
                    chosenPriority = TicketPriority.HIGH;
                    break;
                }
            }
        }
        if (chosenPriority == TicketPriority.MEDIUM) {
            for (String kw : lowKeywords) {
                if (lc.contains(kw)) {
                    chosenPriority = TicketPriority.LOW;
                    break;
                }
            }
        }

        // Confidence heuristic: more matches => higher confidence
        double confidence;
        if (foundKeywords.isEmpty()) {
            confidence = 0.0;
        } else {
            confidence = Math.min(1.0, foundKeywords.size() / 3.0);
        }

        String reasoning = buildReasoning(foundKeywords, chosenCategory, chosenPriority, confidence);

        ClassificationResult result = new ClassificationResult();
        result.setCategory(chosenCategory);
        result.setPriority(chosenPriority);
        result.setConfidence(confidence);
        result.setKeywords(foundKeywords.stream().distinct().collect(Collectors.toList()));
        result.setReasoning(reasoning);

        log.info("Auto-classified ticket {} -> category={}, priority={}, confidence={}, keywords={}",
                ticket.getId(), chosenCategory, chosenPriority, confidence, result.getKeywords());

        return result;
    }

    private String buildReasoning(List<String> keywords, TicketCategory category, TicketPriority priority, double confidence) {
        StringBuilder sb = new StringBuilder();
        sb.append("Matched keywords: ");
        if (keywords.isEmpty()) {
            sb.append("none");
        } else {
            sb.append(String.join(", ", keywords.stream().distinct().collect(Collectors.toList())));
        }
        sb.append("; Chosen category: ").append(category);
        sb.append("; Chosen priority: ").append(priority);
        sb.append(String.format("; Confidence: %.2f", confidence));
        return sb.toString();
    }
}
