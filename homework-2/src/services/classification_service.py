"""
Classification Service - Auto-categorization based on keyword analysis

Strategy:
1. Keyword-based matching (deterministic and testable)
2. No external API dependencies
3. Transparent confidence scoring
4. Disambiguation when multiple categories match
"""

from typing import Dict, List, Tuple
from uuid import UUID

from ..models import Ticket, TicketCategory, TicketPriority, ClassificationResult
from .ticket_service import ticket_service


# Category keyword maps
CATEGORY_KEYWORDS = {
    TicketCategory.ACCOUNT_ACCESS: [
        "login", "password", "access denied", "locked out", "sign in", 
        "authentication", "2fa", "two-factor", "can't log in", "reset password",
        "forgot password", "account locked", "credentials"
    ],
    TicketCategory.BILLING_QUESTION: [
        "invoice", "payment", "charge", "refund", "subscription", "price", 
        "billing", "cost", "fee", "paid", "credit card", "receipt", "transaction"
    ],
    TicketCategory.FEATURE_REQUEST: [
        "feature", "suggestion", "would be nice", "improve", "enhancement", 
        "wish list", "add", "could you", "please add", "request", "new feature"
    ],
    TicketCategory.BUG_REPORT: [
        "bug", "defect", "reproduce", "steps to reproduce", "unexpected behavior", 
        "incorrect result", "regression", "broken", "not working as expected",
        "should work", "supposed to"
    ],
    TicketCategory.TECHNICAL_ISSUE: [
        "error", "crash", "not working", "broken", "failed", "timeout", 
        "slow", "unresponsive", "loading", "performance", "doesn't work",
        "won't start", "stopped working"
    ],
}

# Priority keyword maps
PRIORITY_KEYWORDS = {
    TicketPriority.URGENT: [
        "can't access", "critical", "production down", "security", "emergency",
        "data loss", "urgent", "immediately", "asap", "right now", "down",
        "outage", "breach"
    ],
    TicketPriority.HIGH: [
        "important", "blocking", "asap", "urgent need", "high priority",
        "can't work", "blocker", "soon", "quickly"
    ],
    TicketPriority.LOW: [
        "minor", "cosmetic", "suggestion", "nice to have", "when you get a chance",
        "low priority", "whenever", "not urgent", "eventually"
    ],
}


class ClassificationService:
    """Service for auto-classifying tickets based on content analysis"""
    
    def __init__(self):
        """Initialize classification service"""
        pass
    
    def _find_keywords(self, text: str, keywords: List[str]) -> List[str]:
        """
        Find which keywords from a list appear in the text
        
        Args:
            text: Text to search (case-insensitive)
            keywords: List of keywords to find
            
        Returns:
            List of keywords that were found
        """
        text_lower = text.lower()
        found = []
        
        for keyword in keywords:
            if keyword.lower() in text_lower:
                found.append(keyword)
        
        return found
    
    def _classify_category(self, ticket: Ticket) -> Tuple[TicketCategory, float, List[str]]:
        """
        Classify ticket category based on keyword matching
        
        Disambiguation strategy when multiple categories match:
        1. Specificity wins - bug_report requires reproduction keywords
        2. Keyword count - category with most matches wins
        3. Tie-breaker - prefer earlier category in priority order
        4. Confidence reduction - reduce by 0.1 per additional match
        
        Args:
            ticket: Ticket to classify
            
        Returns:
            Tuple of (category, confidence, keywords_found)
        """
        combined_text = f"{ticket.subject} {ticket.description}"
        
        # Find matches for each category
        matches: Dict[TicketCategory, List[str]] = {}
        for category, keywords in CATEGORY_KEYWORDS.items():
            found = self._find_keywords(combined_text, keywords)
            if found:
                matches[category] = found
        
        # No matches - return OTHER with low confidence
        if not matches:
            return TicketCategory.OTHER, 0.3, []
        
        # Single match - high confidence
        if len(matches) == 1:
            category = list(matches.keys())[0]
            keywords_found = matches[category]
            confidence = min(1.0, 0.6 + (len(keywords_found) * 0.1))
            return category, confidence, keywords_found
        
        # Multiple matches - use disambiguation strategy
        # Sort by keyword count (descending)
        sorted_matches = sorted(matches.items(), key=lambda x: len(x[1]), reverse=True)
        
        # Check for bug_report specificity (requires reproduction keywords)
        bug_specific = ["reproduce", "steps to reproduce", "regression", "unexpected behavior"]
        has_bug_specific = any(
            kw in combined_text.lower() for kw in bug_specific
        )
        
        if TicketCategory.BUG_REPORT in matches and has_bug_specific:
            # Prefer bug_report if it has specific keywords
            keywords_found = matches[TicketCategory.BUG_REPORT]
            confidence = 0.8
            return TicketCategory.BUG_REPORT, confidence, keywords_found
        
        # Use category with most keyword matches
        category = sorted_matches[0][0]
        keywords_found = sorted_matches[0][1]
        
        # Reduce confidence for ambiguity
        confidence = min(1.0, 0.6 + (len(keywords_found) * 0.1))
        confidence -= (len(matches) - 1) * 0.1  # Reduce by 0.1 per additional match
        confidence = max(0.3, confidence)  # Floor at 0.3
        
        return category, confidence, keywords_found
    
    def _classify_priority(self, ticket: Ticket) -> Tuple[TicketPriority, List[str]]:
        """
        Classify ticket priority based on keyword matching
        
        Args:
            ticket: Ticket to classify
            
        Returns:
            Tuple of (priority, keywords_found)
        """
        combined_text = f"{ticket.subject} {ticket.description}"
        
        # Check for urgent keywords first
        urgent_found = self._find_keywords(combined_text, PRIORITY_KEYWORDS[TicketPriority.URGENT])
        if urgent_found:
            return TicketPriority.URGENT, urgent_found
        
        # Check for high priority keywords
        high_found = self._find_keywords(combined_text, PRIORITY_KEYWORDS[TicketPriority.HIGH])
        if high_found:
            return TicketPriority.HIGH, high_found
        
        # Check for low priority keywords
        low_found = self._find_keywords(combined_text, PRIORITY_KEYWORDS[TicketPriority.LOW])
        if low_found:
            return TicketPriority.LOW, low_found
        
        # Default to medium
        return TicketPriority.MEDIUM, []
    
    def classify_ticket(self, ticket: Ticket) -> ClassificationResult:
        """
        Analyze ticket and suggest category and priority
        
        Args:
            ticket: Ticket to classify
            
        Returns:
            ClassificationResult with suggestions and confidence
        """
        # Classify category
        category, confidence, category_keywords = self._classify_category(ticket)
        
        # Classify priority
        priority, priority_keywords = self._classify_priority(ticket)
        
        # Build reasoning
        reasoning_parts = []
        if category_keywords:
            reasoning_parts.append(
                f"Category '{category.value}' suggested based on keywords: {', '.join(category_keywords[:3])}"
            )
        else:
            reasoning_parts.append(f"No specific keywords found, defaulting to '{category.value}'")
        
        if priority_keywords:
            reasoning_parts.append(
                f"Priority '{priority.value}' suggested based on keywords: {', '.join(priority_keywords[:3])}"
            )
        else:
            reasoning_parts.append(f"No priority keywords found, defaulting to '{priority.value}'")
        
        reasoning = ". ".join(reasoning_parts)
        
        return ClassificationResult(
            ticket_id=ticket.id,
            suggested_category=category,
            suggested_priority=priority,
            confidence=confidence,
            reasoning=reasoning,
            keywords_found=category_keywords + priority_keywords,
        )
    
    def auto_classify_all(self) -> List[ClassificationResult]:
        """
        Auto-classify all tickets
        
        Returns:
            List of classification results for all tickets
        """
        all_tickets = ticket_service.get_all_tickets()
        results = []
        
        for ticket in all_tickets:
            result = self.classify_ticket(ticket)
            results.append(result)
        
        return results
    
    def apply_classification(self, ticket_id: UUID, classification: ClassificationResult) -> bool:
        """
        Apply classification result to a ticket (update category and priority)
        
        Args:
            ticket_id: UUID of ticket to update
            classification: Classification result to apply
            
        Returns:
            True if successful, False if ticket not found
        """
        from ..models import TicketUpdate
        
        update_data = TicketUpdate(
            category=classification.suggested_category,
            priority=classification.suggested_priority,
        )
        
        updated_ticket = ticket_service.update_ticket(ticket_id, update_data)
        return updated_ticket is not None


# Global service instance
classification_service = ClassificationService()
