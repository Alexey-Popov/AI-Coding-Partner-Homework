#!/bin/bash

# Run sample HTTP requests against the Banking Transactions API
# Requires: curl (and optionally jq for pretty JSON)

BASE_URL="http://localhost:3000"

echo "Running sample requests against $BASE_URL"

echo "\n1) Health check"
curl -s "$BASE_URL/health" | (jq || cat)

echo "\n2) API info"
curl -s "$BASE_URL/" | (jq || cat)

echo "\n3) Create a deposit transaction"
CREATE_RESP=$(curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{"toAccount":"ACC-12345","amount":1000.00,"currency":"USD","type":"deposit"}')

echo "$CREATE_RESP" | (jq || cat)

TRANSACTION_ID=$(echo "$CREATE_RESP" | (jq -r '.data.id' 2>/dev/null) || echo "")

echo "\n4) List transactions"
curl -s "$BASE_URL/transactions" | (jq || cat)

echo "\n5) Get specific transaction (created above)"
if [ -n "$TRANSACTION_ID" ] && [ "$TRANSACTION_ID" != "null" ]; then
  curl -s "$BASE_URL/transactions/$TRANSACTION_ID" | (jq || cat)
else
  echo "No transaction ID available from create response; skipping."
fi

echo "\n6) Export CSV"
echo "(raw CSV response)"
curl -s "$BASE_URL/transactions/export?format=csv" | sed -n '1,10p'

echo "\n7) Get account balance"
curl -s "$BASE_URL/accounts/ACC-12345/balance" | (jq || cat)

echo "\nDone."
