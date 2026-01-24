# ▶️ How to Run the Banking Transactions API

## 📋 Prerequisites

- **Node.js** 18.0+ ([download](https://nodejs.org/))
- **npm** 9.0+ (comes with Node.js)

**Optional:** curl, Postman, or VS Code REST Client for testing

---

## 🚀 Quick Start

**1. Install dependencies:**
```bash
npm install
```

**2. Start the server:**
```bash
npm start
```

**3. Verify it's running:**
```bash
curl http://localhost:3000/
```

The API is now available at `http://localhost:3000`

---

## 🧪 Testing the API

### Option A: Demo Script

```bash
chmod +x demo/run.sh && ./demo/run.sh
```

### Option B: Manual Testing

```bash
# Create a deposit
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{"toAccount":"ACC-12345","amount":1000,"currency":"USD","type":"deposit"}'

# Get all transactions
curl http://localhost:3000/transactions

# Get account balance
curl http://localhost:3000/accounts/ACC-12345/balance
```

### Option C: VS Code REST Client

Open `demo/sample-requests.http` and click "Send Request" above any request.

---

## 📚 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | API documentation |
| POST | `/transactions` | Create transaction |
| GET | `/transactions` | List transactions (supports filters) |
| GET | `/transactions/:id` | Get transaction by ID |
| GET | `/transactions/export?format=csv` | Export to CSV |
| GET | `/accounts/:accountId/balance` | Get balance |
| GET | `/accounts/:accountId/summary` | Get summary |

---

## 📖 Transaction Types & Validation

### Transaction Type Rules

| Type | fromAccount | toAccount |
|------|-------------|-----------|
| `deposit` | ❌ Not allowed | ✅ Required |
| `withdrawal` | ✅ Required | ❌ Not allowed |
| `transfer` | ✅ Required | ✅ Required |

### Validation Rules

- **Amount**: Positive number, max 2 decimal places
- **Currency**: Valid ISO 4217 code (USD, EUR, GBP, JPY, CHF, CAD, AUD, etc.)
- **Account Format**: `ACC-XXXXX` (5 alphanumeric characters)

### Query Parameters (Filtering)

- `accountId` - Filter by account ID
- `type` - Filter by transaction type (deposit/withdrawal/transfer)
- `from` - Start date (YYYY-MM-DD or ISO 8601)
- `to` - End date (YYYY-MM-DD or ISO 8601)

---

## ⚠️ Error Responses

All errors return consistent JSON:

```json
{
  "error": "Error message",
  "details": [{"field": "fieldName", "message": "Specific error"}]
}
```

**HTTP Status Codes:**
- `200` OK - Successful GET
- `201` Created - Transaction created
- `400` Bad Request - Validation error
- `404` Not Found - Resource not found
- `429` Too Many Requests - Rate limit exceeded (100 req/min)
- `500` Internal Server Error

---

## 🐛 Troubleshooting

**Port 3000 in use:**
```bash
lsof -ti:3000 | xargs kill -9
```

**Module errors:**
```bash
rm -rf node_modules package-lock.json && npm install
```

**Cannot connect:**
- Verify server is running
- Check `http://127.0.0.1:3000` instead
- Ensure firewall allows port 3000

---

## 📦 npm Scripts

| Command | Description |
|---------|-------------|
| `npm start` | Run with ts-node |
| `npm run dev` | Same as start |
| `npm run build` | Compile to JavaScript |

---

## 💡 Tips

- Use `jq` for pretty JSON: `curl http://localhost:3000/transactions | jq`
- Check `demo/sample-requests.http` for 59 ready-to-use examples
- Rate limit: 100 requests/minute per IP
- All accounts start at $0 balance

For detailed architecture and implementation notes, see [README.md](README.md).

---

<div align="center">

**Banking Transactions API v1.0.0**

</div>
