#!/bin/bash

# Demo Script: Import Sample Data
# This script imports all the sample data files into the ticket system
# - 50 tickets from CSV
# - 20 tickets from JSON
# - 30 tickets from XML
# Total: 100 sample tickets

set -e  # Exit on error

BASE_URL="http://localhost:3000"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo ""
echo "========================================="
echo "🎫 Ticket System - Sample Data Import"
echo "========================================="
echo ""

# Check if server is running
echo -n "Checking if server is running... "
if curl -s "${BASE_URL}/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Server is up${NC}"
else
    echo -e "${RED}✗ Server is not running${NC}"
    echo ""
    echo "Please start the server first:"
    echo "  cd homework-2 && npm run dev"
    echo ""
    echo "Or use the task:"
    echo "  Run Task: hw2: start server (3000)"
    exit 1
fi
echo ""

# Function to parse and display import results
parse_result() {
    python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(f\"  Format: {data.get('format', 'N/A')}\")
    print(f\"  Total: {data.get('total', 0)}\")
    print(f\"  Imported: {data.get('imported', 0)}\")
    print(f\"  Failed: {data.get('failed', 0)}\")
    if data.get('failed', 0) > 0 and 'errors' in data:
        print(f\"  Errors: {len(data['errors'])} validation errors\")
except:
    print('  Error parsing response')
    sys.exit(1)
"
}

# 1. Import CSV (50 tickets)
echo -e "${BLUE}[1/3] Importing CSV file (50 tickets)...${NC}"
if [ -f "sample_tickets.csv" ]; then
    curl -s -X POST "${BASE_URL}/tickets/import" \
        -F "file=@sample_tickets.csv" \
        | parse_result
    echo -e "${GREEN}✓ CSV import completed${NC}"
else
    echo -e "${RED}✗ docs/sample/sample_tickets.csv not found${NC}"
    exit 1
fi
echo ""

# 2. Import JSON (20 tickets)
echo -e "${BLUE}[2/3] Importing JSON file (20 tickets)...${NC}"
if [ -f "sample_tickets.json" ]; then
    curl -s -X POST "${BASE_URL}/tickets/import" \
        -F "file=@sample_tickets.json" \
        | parse_result
    echo -e "${GREEN}✓ JSON import completed${NC}"
else
    echo -e "${RED}✗ docs/sample/sample_tickets.json not found${NC}"
    exit 1
fi
echo ""

# 3. Import XML (30 tickets)
echo -e "${BLUE}[3/3] Importing XML file (30 tickets)...${NC}"
if [ -f "sample_tickets.xml" ]; then
    curl -s -X POST "${BASE_URL}/tickets/import" \
        -F "file=@sample_tickets.xml" \
        | parse_result
    echo -e "${GREEN}✓ XML import completed${NC}"
else
    echo -e "${RED}✗ docs/sample/sample_tickets.xml not found${NC}"
    exit 1
fi
echo ""

echo "========================================="
echo -e "${GREEN}📊 Import Summary${NC}"
echo "========================================="

# Get ticket counts by category
echo ""
echo "Ticket Statistics:"
TOTAL=$(curl -s "${BASE_URL}/tickets" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    tickets = data.get('data', [])
    print(f'  Total Tickets: {len(tickets)}')
    
    # Count by category
    categories = {}
    for t in tickets:
        cat = t.get('classification', {}).get('category', 'other')
        categories[cat] = categories.get(cat, 0) + 1
    
    print('\\n  By Category:')
    for cat, count in sorted(categories.items()):
        print(f'    - {cat}: {count}')
    
    # Count by priority
    priorities = {}
    for t in tickets:
        pri = t.get('classification', {}).get('priority', 'medium')
        priorities[pri] = priorities.get(pri, 0) + 1
    
    print('\\n  By Priority:')
    for pri in ['urgent', 'high', 'medium', 'low']:
        if pri in priorities:
            print(f'    - {pri}: {priorities[pri]}')
except Exception as e:
    print(f'  Error: {e}')
")

echo ""
echo "========================================="
echo ""
echo -e "${GREEN}✅ All sample data imported successfully!${NC}"
echo ""
echo "Next steps:"
echo "  • View all tickets: curl http://localhost:3000/tickets"
echo "  • Filter by priority: curl 'http://localhost:3000/tickets?priority=urgent'"
echo "  • Filter by category: curl 'http://localhost:3000/tickets?category=technical_issue'"
echo "  • Run tests: npm test"
echo "  • View documentation: docs/"
echo ""
