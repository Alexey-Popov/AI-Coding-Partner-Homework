"""
Classification decision logger.

Logs every auto-classification decision to a structured JSON log file
so that decisions can be audited later.  Uses Python's built-in logging
module with a JSON formatter.
"""

import json
import logging
import os
from datetime import datetime, timezone

# ---------------------------------------------------------------------------
# Logger setup
# ---------------------------------------------------------------------------

# Log file lives alongside the application (configurable via env var)
_LOG_DIR = os.environ.get("CLASSIFY_LOG_DIR", "logs")
_LOG_FILE = os.path.join(_LOG_DIR, "classification_decisions.log")

# Create the log directory if it doesn't exist
os.makedirs(_LOG_DIR, exist_ok=True)

# Dedicated logger so classification logs don't mix with Flask's logs
_logger = logging.getLogger("classification")
_logger.setLevel(logging.INFO)

# Avoid adding duplicate handlers if this module is re-imported
if not _logger.handlers:
    _handler = logging.FileHandler(_LOG_FILE)
    _handler.setLevel(logging.INFO)
    # Plain formatter — we write structured JSON ourselves
    _handler.setFormatter(logging.Formatter("%(message)s"))
    _logger.addHandler(_handler)


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def log_classification_decision(ticket_id: str, result: dict) -> None:
    """
    Write a JSON log entry for a classification decision.

    Each line in the log file is a self-contained JSON object with:
      - timestamp (ISO-8601 UTC)
      - ticket_id
      - category, priority, confidence
      - reasoning
      - keywords_found
    """
    entry = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "ticket_id": ticket_id,
        "category": result.get("category"),
        "priority": result.get("priority"),
        "confidence": result.get("confidence"),
        "reasoning": result.get("reasoning"),
        "keywords_found": result.get("keywords_found", []),
    }
    _logger.info(json.dumps(entry))
