"""
Classification service — automatic ticket categorization and priority assignment.

Uses keyword matching against the ticket's subject and description to determine
the most likely category and priority.  Returns a confidence score (0-1),
human-readable reasoning, and the list of keywords that triggered the decision.
"""

from datetime import datetime, timezone

from app.utils.classification_logger import log_classification_decision


# ---------------------------------------------------------------------------
# Keyword dictionaries
# ---------------------------------------------------------------------------

# Each category maps to a list of keywords / phrases.
# Phrases are checked as substrings (case-insensitive).
CATEGORY_KEYWORDS: dict[str, list[str]] = {
    "account_access": [
        "login", "log in", "password", "2fa", "two-factor", "two factor",
        "sign in", "signin", "locked out", "access denied", "can't access",
        "cannot access", "authentication", "reset password", "account locked",
        "mfa", "multi-factor",
    ],
    "technical_issue": [
        "bug", "error", "crash", "broken", "not working", "exception",
        "timeout", "glitch", "freeze", "hang", "slow", "unresponsive",
        "500", "404", "server error", "white screen", "blank page",
    ],
    "billing_question": [
        "payment", "invoice", "refund", "charge", "billing", "subscription",
        "pricing", "receipt", "overcharged", "double charged", "credit card",
        "plan", "upgrade", "downgrade", "cancel subscription", "renewal",
    ],
    "feature_request": [
        "feature", "enhancement", "suggestion", "improve", "add support",
        "wish", "request", "would be nice", "it would be great",
        "please add", "new feature", "could you add",
    ],
    "bug_report": [
        "defect", "reproduce", "steps to reproduce", "regression",
        "unexpected behavior", "unexpected behaviour", "repro steps",
        "consistently fails", "intermittent failure", "stack trace",
    ],
}

# Priority keywords — checked independently of category.
PRIORITY_KEYWORDS: dict[str, list[str]] = {
    "urgent": [
        "can't access", "cannot access", "critical", "production down",
        "security", "data breach", "data loss", "emergency", "outage",
        "system down",
    ],
    "high": [
        "important", "blocking", "asap", "as soon as possible", "urgent need",
        "high priority", "blocker", "showstopper",
    ],
    "low": [
        "minor", "cosmetic", "suggestion", "low priority", "nice to have",
        "when you get a chance", "not urgent", "trivial",
    ],
}

# Weights control how much each keyword match contributes to the confidence
# score.  More specific (longer) phrases earn higher weight.
_BASE_WEIGHT = 0.20          # weight per short keyword match
_LONG_PHRASE_WEIGHT = 0.30   # weight for phrases with 2+ words
_CONFIDENCE_CAP = 1.0        # maximum confidence score
_HIGH_CONFIDENCE_THRESHOLD = 0.70  # threshold label for reasoning


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def classify_ticket(ticket_dict: dict) -> dict:
    """
    Classify a ticket and return the classification result.

    *ticket_dict* must contain at least ``subject`` and ``description``.

    Returns a dict with keys:
        category, priority, confidence, reasoning, keywords_found
    """
    text = _build_searchable_text(ticket_dict)

    # Determine category
    category, cat_keywords, cat_score = _match_category(text)

    # Determine priority
    priority, pri_keywords, pri_score = _match_priority(text)

    # Combine all matched keywords
    all_keywords = sorted(set(cat_keywords + pri_keywords))

    # Overall confidence is the average of category and priority confidence,
    # weighted towards category since it's the harder classification.
    confidence = round(min(_CONFIDENCE_CAP, cat_score * 0.7 + pri_score * 0.3), 2)

    # Build human-readable reasoning
    reasoning = _build_reasoning(category, priority, cat_keywords, pri_keywords, confidence)

    result = {
        "category": category,
        "priority": priority,
        "confidence": confidence,
        "reasoning": reasoning,
        "keywords_found": all_keywords,
    }

    # Log the decision
    log_classification_decision(
        ticket_id=ticket_dict.get("id", "unknown"),
        result=result,
    )

    return result


# ---------------------------------------------------------------------------
# Internal helpers
# ---------------------------------------------------------------------------

def _build_searchable_text(ticket: dict) -> str:
    """Combine subject and description into a single lowercase string for matching."""
    subject = (ticket.get("subject") or "").lower()
    description = (ticket.get("description") or "").lower()
    return f"{subject} {description}"


def _match_category(text: str) -> tuple[str, list[str], float]:
    """
    Find the best-matching category for *text*.

    Returns (category_name, matched_keywords, confidence_score).
    """
    best_category = "other"
    best_keywords: list[str] = []
    best_score = 0.0

    for category, keywords in CATEGORY_KEYWORDS.items():
        matched, score = _score_keywords(text, keywords)
        if score > best_score:
            best_score = score
            best_category = category
            best_keywords = matched

    return best_category, best_keywords, min(_CONFIDENCE_CAP, best_score)


def _match_priority(text: str) -> tuple[str, list[str], float]:
    """
    Find the best-matching priority for *text*.

    If no priority keywords match, defaults to "medium".
    Returns (priority_name, matched_keywords, confidence_score).
    """
    best_priority = "medium"
    best_keywords: list[str] = []
    best_score = 0.0

    for priority, keywords in PRIORITY_KEYWORDS.items():
        matched, score = _score_keywords(text, keywords)
        if score > best_score:
            best_score = score
            best_priority = priority
            best_keywords = matched

    # Medium is the default — if nothing matched, return medium with full confidence
    if best_priority == "medium" and best_score == 0.0:
        return "medium", [], 1.0

    return best_priority, best_keywords, min(_CONFIDENCE_CAP, best_score)


def _score_keywords(text: str, keywords: list[str]) -> tuple[list[str], float]:
    """
    Score *text* against a list of *keywords*.

    Returns (matched_keyword_list, cumulative_score).
    Multi-word phrases get a higher weight.
    """
    matched: list[str] = []
    score = 0.0

    for kw in keywords:
        if kw in text:
            matched.append(kw)
            # Multi-word phrases are more specific → higher weight
            weight = _LONG_PHRASE_WEIGHT if " " in kw else _BASE_WEIGHT
            score += weight

    return matched, score


def _build_reasoning(
    category: str,
    priority: str,
    cat_keywords: list[str],
    pri_keywords: list[str],
    confidence: float,
) -> str:
    """Build a human-readable explanation of the classification decision."""
    parts: list[str] = []

    # Category reasoning
    if cat_keywords:
        kw_str = ", ".join(f'"{kw}"' for kw in cat_keywords)
        parts.append(
            f"Category set to '{category}' based on keyword matches: {kw_str}."
        )
    else:
        parts.append(
            f"Category set to '{category}' (default) — no strong keyword matches found."
        )

    # Priority reasoning
    if pri_keywords:
        kw_str = ", ".join(f'"{kw}"' for kw in pri_keywords)
        parts.append(
            f"Priority set to '{priority}' based on keyword matches: {kw_str}."
        )
    else:
        parts.append(
            f"Priority set to '{priority}' (default) — no priority keywords matched."
        )

    # Confidence note
    if confidence >= _HIGH_CONFIDENCE_THRESHOLD:
        parts.append(f"Confidence: {confidence} (high).")
    elif confidence >= 0.4:
        parts.append(f"Confidence: {confidence} (medium).")
    else:
        parts.append(f"Confidence: {confidence} (low) — manual review recommended.")

    return " ".join(parts)
