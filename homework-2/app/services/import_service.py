"""
Import service — multi-format ticket bulk import.

Parses CSV, JSON, and XML payloads into ticket dicts, validates each record
individually, and returns a summary of successes and failures.
"""

import csv
import io
import json
import xml.etree.ElementTree as ET

from app.services.ticket_service import create_ticket


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def import_tickets(file_content: bytes, file_format: str) -> dict:
    """
    Parse *file_content* according to *file_format* and create tickets.

    Supported formats: "csv", "json", "xml".

    Returns a summary dict:
    {
        "total": int,
        "successful": int,
        "failed": int,
        "errors": [ { "row": int, "errors": [...] } ]
    }
    """
    parser = _PARSERS.get(file_format)
    if parser is None:
        return {
            "total": 0,
            "successful": 0,
            "failed": 0,
            "errors": [{"row": 0, "errors": [f"Unsupported format: {file_format}"]}],
        }

    try:
        records = parser(file_content)
    except Exception as exc:
        return {
            "total": 0,
            "successful": 0,
            "failed": 0,
            "errors": [{"row": 0, "errors": [f"Failed to parse file: {str(exc)}"]}],
        }

    return _process_records(records)


# ---------------------------------------------------------------------------
# Format-specific parsers
# ---------------------------------------------------------------------------

def _parse_csv(content: bytes) -> list[dict]:
    """
    Parse CSV bytes into a list of ticket dicts.

    Expects a header row with field names matching the ticket model.
    The 'tags' field is expected as a semicolon-separated string.
    Metadata fields use dot notation: metadata.source, metadata.browser, etc.
    """
    text = content.decode("utf-8-sig")  # handle BOM
    reader = csv.DictReader(io.StringIO(text))
    records: list[dict] = []

    for row in reader:
        record = {}
        metadata = {}

        for key, value in row.items():
            if key is None:
                continue
            key = key.strip()
            value = value.strip() if value else ""

            if key == "tags":
                # Semicolon-separated list
                record["tags"] = [t.strip() for t in value.split(";") if t.strip()] if value else []
            elif key.startswith("metadata."):
                # Flatten dotted keys into a metadata dict
                sub_key = key.split(".", 1)[1]
                metadata[sub_key] = value
            else:
                record[key] = value

        if metadata:
            record["metadata"] = metadata

        records.append(record)

    return records


def _parse_json(content: bytes) -> list[dict]:
    """
    Parse JSON bytes into a list of ticket dicts.

    Accepts either a JSON array of objects or a JSON object with a
    top-level "tickets" key containing the array.
    """
    data = json.loads(content.decode("utf-8"))

    if isinstance(data, list):
        return data
    if isinstance(data, dict) and "tickets" in data:
        return data["tickets"]

    raise ValueError("JSON must be an array of tickets or an object with a 'tickets' key")


def _parse_xml(content: bytes) -> list[dict]:
    """
    Parse XML bytes into a list of ticket dicts.

    Expected structure:
    <tickets>
      <ticket>
        <customer_id>...</customer_id>
        <tags>
          <tag>tag1</tag>
          <tag>tag2</tag>
        </tags>
        <metadata>
          <source>web_form</source>
          <browser>Chrome</browser>
          <device_type>desktop</device_type>
        </metadata>
        ...
      </ticket>
    </tickets>
    """
    root = ET.fromstring(content)

    # Accept both a <tickets> wrapper and a flat list of <ticket> elements
    ticket_elements = root.findall("ticket") if root.tag == "tickets" else [root] if root.tag == "ticket" else []

    records: list[dict] = []
    for elem in ticket_elements:
        record: dict = {}
        for child in elem:
            if child.tag == "tags":
                record["tags"] = [tag.text.strip() for tag in child.findall("tag") if tag.text]
            elif child.tag == "metadata":
                meta = {}
                for meta_child in child:
                    if meta_child.text:
                        meta[meta_child.tag] = meta_child.text.strip()
                record["metadata"] = meta
            else:
                record[child.tag] = child.text.strip() if child.text else ""
        records.append(record)

    return records


# Map of supported format names to their parser functions
_PARSERS = {
    "csv": _parse_csv,
    "json": _parse_json,
    "xml": _parse_xml,
}


# ---------------------------------------------------------------------------
# Internal helpers
# ---------------------------------------------------------------------------

def _process_records(records: list[dict]) -> dict:
    """
    Iterate over parsed records, create each as a ticket, and collect results.

    Returns the import summary dict.
    """
    total = len(records)
    successful = 0
    failed = 0
    error_details: list[dict] = []

    for idx, record in enumerate(records, start=1):
        ticket, errors = create_ticket(record)
        if errors:
            failed += 1
            error_details.append({"row": idx, "errors": errors})
        else:
            successful += 1

    return {
        "total": total,
        "successful": successful,
        "failed": failed,
        "errors": error_details,
    }
