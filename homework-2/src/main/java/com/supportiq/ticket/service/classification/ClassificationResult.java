package com.supportiq.ticket.service.classification;

import com.supportiq.ticket.enums.TicketCategory;
import com.supportiq.ticket.enums.TicketPriority;

import java.util.List;

public record ClassificationResult(
        TicketCategory category,
        TicketPriority priority,
        double confidence,
        String reasoning,
        List<String> matchedKeywords
) {}
