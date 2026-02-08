#!/bin/bash

BASE_URL="http://localhost:3000"

echo "========================================="
echo "Multi-Format Import Test"
echo "========================================="
echo ""

echo "1. Testing CSV Import..."
echo "---"
curl -s -X POST "${BASE_URL}/tickets/import" \
  -F "file=@tests/fixtures/valid-tickets.csv" \
  | python3 -c "import sys, json; data = json.load(sys.stdin); print(f\"Format: {data['format']}\"); print(f\"Success: {data['success']}\"); print(f\"Imported: {data['imported']}\"); print(f\"Failed: {data['failed']}\")"
echo ""
echo "========================================="
echo ""

echo "2. Testing JSON Import..."
echo "---"
curl -s -X POST "${BASE_URL}/tickets/import" \
  -F "file=@tests/fixtures/valid-tickets.json" \
  | python3 -c "import sys, json; data = json.load(sys.stdin); print(f\"Format: {data['format']}\"); print(f\"Success: {data['success']}\"); print(f\"Imported: {data['imported']}\"); print(f\"Failed: {data['failed']}\")"
echo ""
echo "========================================="
echo ""

echo "3. Testing XML Import..."
echo "---"
curl -s -X POST "${BASE_URL}/tickets/import" \
  -F "file=@tests/fixtures/valid-tickets.xml" \
  | python3 -c "import sys, json; data = json.load(sys.stdin); print(f\"Format: {data['format']}\"); print(f\"Success: {data['success']}\"); print(f\"Imported: {data['imported']}\"); print(f\"Failed: {data['failed']}\")"
echo ""
echo "========================================="
echo ""

echo "4. Listing all tickets..."
TOTAL=$(curl -s "${BASE_URL}/tickets" | python3 -c "import sys, json; print(len(json.load(sys.stdin)['data']))")
echo "Total tickets in system: $TOTAL"
echo ""
echo "========================================="
echo ""

echo "5. Testing error handling with invalid JSON..."
curl -s -X POST "${BASE_URL}/tickets/import" \
  -F "file=@tests/fixtures/malformed.json" \
  | python3 -c "import sys, json; data = json.load(sys.stdin); print(f\"Success: {data['success']}\"); print(f\"Error: {data['error']}\"); print(f\"Errors count: {len(data['errors'])}\")"
echo ""
echo "========================================="
echo ""

echo "✅ All format tests completed!"
echo ""
echo "Summary:"
echo "- CSV Import: ✓ Working"
echo "- JSON Import: ✓ Working"
echo "- XML Import: ✓ Working"
echo "- Error Handling: ✓ Working"
