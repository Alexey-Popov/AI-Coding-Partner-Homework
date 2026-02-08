"""
Ticket API routes.

Blueprint that exposes all REST endpoints for managing support tickets:
  POST   /tickets                    – create a single ticket
  POST   /tickets/import             – bulk import from CSV / JSON / XML
  POST   /tickets/<id>/auto-classify – auto-classify a ticket
  GET    /tickets                    – list tickets (with optional filters)
  GET    /tickets/<id>               – retrieve one ticket
  PUT    /tickets/<id>               – update a ticket (manual override)
  DELETE /tickets/<id>               – delete a ticket
"""

from flask import Blueprint, jsonify, request

from app.services import ticket_service, import_service

tickets_bp = Blueprint("tickets", __name__)

# Mapping of MIME types and extensions to internal format names
_FORMAT_MAP = {
    "text/csv": "csv",
    "application/json": "json",
    "text/xml": "xml",
    "application/xml": "xml",
    "csv": "csv",
    "json": "json",
    "xml": "xml",
}


# ---------------------------------------------------------------------------
# POST /tickets  — create a single ticket
# ---------------------------------------------------------------------------

@tickets_bp.route("/tickets", methods=["POST"])
def create_ticket():
    """
    Create a new support ticket from a JSON body.

    Accepts an optional ``auto_classify`` query parameter or JSON field.
    When set to "true" / true, the ticket is automatically classified
    after creation and its category/priority may be updated.
    """
    data = request.get_json(silent=True)
    if data is None:
        return jsonify({"error": "Request body must be valid JSON"}), 400

    # Check for auto_classify flag in query param or request body
    auto_classify = (
        request.args.get("auto_classify", "").lower() == "true"
        or data.pop("auto_classify", False) is True
    )

    ticket, errors = ticket_service.create_ticket(data, auto_classify=auto_classify)
    if errors:
        return jsonify({"error": "Validation failed", "details": errors}), 400

    return jsonify(ticket), 201


# ---------------------------------------------------------------------------
# POST /tickets/import  — bulk import from file
# ---------------------------------------------------------------------------

@tickets_bp.route("/tickets/import", methods=["POST"])
def import_tickets():
    """
    Bulk-import tickets from an uploaded file.

    The file format is determined by (in priority order):
      1. The 'format' query parameter (?format=csv)
      2. The uploaded file's extension
      3. The request Content-Type header

    The file should be sent as a multipart form upload with field name 'file',
    or as raw body content with an appropriate Content-Type.
    """
    file_content = None
    file_format = None

    # --- Determine format from query param ---
    explicit_format = request.args.get("format", "").lower()
    if explicit_format in _FORMAT_MAP:
        file_format = _FORMAT_MAP[explicit_format]

    # --- Try multipart file upload first ---
    if "file" in request.files:
        uploaded = request.files["file"]
        file_content = uploaded.read()

        # Detect format from extension if not already set
        if file_format is None and uploaded.filename:
            ext = uploaded.filename.rsplit(".", 1)[-1].lower() if "." in uploaded.filename else ""
            file_format = _FORMAT_MAP.get(ext)
    else:
        # Fall back to raw request body
        file_content = request.get_data()

    # Detect format from Content-Type if still unknown
    if file_format is None:
        content_type = (request.content_type or "").split(";")[0].strip().lower()
        file_format = _FORMAT_MAP.get(content_type)

    # Validate we have content and a format
    if not file_content:
        return jsonify({"error": "No file content provided"}), 400
    if file_format is None:
        return jsonify({
            "error": "Could not determine file format. "
                     "Use the 'format' query parameter (csv, json, xml), "
                     "upload a file with an appropriate extension, or set the Content-Type header."
        }), 400

    summary = import_service.import_tickets(file_content, file_format)
    # Return 400 if every record failed, 200 otherwise (partial success is OK)
    status_code = 400 if summary["total"] > 0 and summary["successful"] == 0 else 200
    return jsonify(summary), status_code


# ---------------------------------------------------------------------------
# POST /tickets/<id>/auto-classify  — auto-classify a ticket
# ---------------------------------------------------------------------------

@tickets_bp.route("/tickets/<ticket_id>/auto-classify", methods=["POST"])
def auto_classify_ticket(ticket_id: str):
    """
    Run the auto-classification engine on an existing ticket.

    Analyses the ticket's subject and description, then updates the ticket's
    category and priority based on keyword matching.  Returns the classification
    result including confidence score, reasoning, and matched keywords.
    """
    ticket, result = ticket_service.auto_classify_ticket(ticket_id)

    if ticket is None:
        return jsonify({"error": "Ticket not found"}), 404

    return jsonify({
        "ticket": ticket,
        "classification": result,
    }), 200


# ---------------------------------------------------------------------------
# GET /tickets  — list tickets with optional filtering
# ---------------------------------------------------------------------------

@tickets_bp.route("/tickets", methods=["GET"])
def list_tickets():
    """
    Return all tickets, optionally filtered by query parameters.

    Supported filters: status, priority, category, assigned_to, customer_email.
    """
    allowed_filters = ["status", "priority", "category", "assigned_to", "customer_email"]
    filters = {k: request.args[k] for k in allowed_filters if k in request.args}

    tickets = ticket_service.get_all_tickets(filters if filters else None)
    return jsonify({"tickets": tickets, "count": len(tickets)}), 200


# ---------------------------------------------------------------------------
# GET /tickets/<id>  — get one ticket
# ---------------------------------------------------------------------------

@tickets_bp.route("/tickets/<ticket_id>", methods=["GET"])
def get_ticket(ticket_id: str):
    """Return a single ticket by its UUID."""
    ticket = ticket_service.get_ticket(ticket_id)
    if ticket is None:
        return jsonify({"error": "Ticket not found"}), 404
    return jsonify(ticket), 200


# ---------------------------------------------------------------------------
# PUT /tickets/<id>  — update a ticket
# ---------------------------------------------------------------------------

@tickets_bp.route("/tickets/<ticket_id>", methods=["PUT"])
def update_ticket(ticket_id: str):
    """Apply a partial update to an existing ticket."""
    data = request.get_json(silent=True)
    if data is None:
        return jsonify({"error": "Request body must be valid JSON"}), 400

    ticket, errors = ticket_service.update_ticket(ticket_id, data)

    # ticket is None when not found
    if ticket is None:
        return jsonify({"error": "Ticket not found"}), 404
    if errors:
        return jsonify({"error": "Validation failed", "details": errors}), 400

    return jsonify(ticket), 200


# ---------------------------------------------------------------------------
# DELETE /tickets/<id>  — delete a ticket
# ---------------------------------------------------------------------------

@tickets_bp.route("/tickets/<ticket_id>", methods=["DELETE"])
def delete_ticket(ticket_id: str):
    """Delete a ticket by its UUID."""
    deleted = ticket_service.delete_ticket(ticket_id)
    if not deleted:
        return jsonify({"error": "Ticket not found"}), 404
    return jsonify({"message": "Ticket deleted successfully"}), 200
