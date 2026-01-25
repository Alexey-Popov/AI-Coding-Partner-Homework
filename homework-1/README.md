# 🏦 Homework 1: Banking Transactions API

> **Student Name**: [Denys Kobernik]
> **Date Submitted**: [25 Jan 2026]
> **AI Tools Used**: [GitHub Copilot]

---

## 📋 Project Overview

It's a CRUD REST API for managing transaction entities. 
The API allows users to create, retrieve, and list transactions, as well as check account balance.

It was implemented with a help of GitHub Copilot AI coding assistant.
Initial description of tasks was copied in a separate instruction.md file with the following prompt and AI was asked to execute the instruction.
As the last step, Task 4 was implemented with the 2nd prompt.

```
As an expert in Spring Boot, Java, REST, plan out the implementation of the following tasks: 
1. Task 1
2. Task 2
3. Task 3
DO NOT implement anyhting without validation and confirmation of the plan.
ASK additional questions to clarify details.
```

## How to Test the API

### Using cURL

#### 1. Create a deposit transaction
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 1000.00,
    "currency": "USD",
    "type": "DEPOSIT",
    "status": "COMPLETED"
  }'
```

#### 2. Create a withdrawal transaction
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "amount": 250.50,
    "currency": "USD",
    "type": "WITHDRAWAL",
    "status": "COMPLETED"
  }'
```

#### 3. Get all transactions
```bash
curl http://localhost:8080/transactions
```

#### 4. Get account balance
```bash
curl http://localhost:8080/accounts/ACC-12345/balance
```

Expected response:
```json
{
  "accountId": "ACC-12345",
  "balance": 749.50,
  "currency": "USD"
}
```

#### 5. Filter transactions by account
```bash
curl "http://localhost:8080/transactions?accountId=ACC-12345"
```

#### 6. Filter by type and date
```bash
curl "http://localhost:8080/transactions?type=DEPOSIT&from=2024-01-01&to=2024-12-31"
```

