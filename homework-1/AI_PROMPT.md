# Prompt for AI Coding Assistant

**Role:** Senior Java Developer / Micronaut Expert
**Task:** Implement a Banking Transactions REST API
**Tech Stack:** Java 25, Micronaut, Gradle

---

## Instructions

Please generate a complete, working REST API application using **Micronaut** and **Java 25** with **Gradle**. The application should manage banking transactions using **in-memory storage** (no database required).

### 1. Data Model
Create a `Transaction` model with the following fields:
- `id` (String, auto-generated UUID)
- `fromAccount` (String)
- `toAccount` (String)
- `amount` (BigDecimal)
- `currency` (String, ISO 4217)
- `type` (String: "deposit", "withdrawal", "transfer")
- `timestamp` (LocalDateTime)
- `status` (String: "pending", "completed", "failed")

### 2. Core Endpoints
Implement the following REST endpoints:
- **POST /transactions**: Create a new transaction. Returns 201 on success.
- **GET /transactions**: List all transactions. Supports query parameters for filtering:
    - `accountId`: Filter by account (either sender or receiver).
    - `type`: Filter by transaction type.
    - `from/to`: Filter by date range.
- **GET /transactions/{id}**: Get details of a specific transaction. Returns 404 if not found.
- **GET /accounts/{accountId}/balance**: Calculate current balance based on transaction history.

### 3. Validation Logic
Implement strict validation with HTTP 400 error responses:
- **Amount**: Must be positive and have max 2 decimal places.
- **Account Format**: Must match regex `^ACC-[a-zA-Z0-9]+$`.
- **Currency**: Must be a valid ISO 4217 code.
- Provide meaningful error messages (e.g., "Amount must be positive").

### 4. Additional Feature: Transaction Summary
Implement the following analytics endpoint:
- **GET /accounts/{accountId}/summary**: Returns a JSON object with:
    - Total sum of deposits
    - Total sum of withdrawals
    - Total count of transactions
    - Date of the most recent transaction

### 5. Project Structure & Configuration
- Provide a `build.gradle` file configured for Java 25 and the latest Micronaut version.
- Use Micronaut's built-in Validation (`jakarta.validation`).
- Organize code into `controller`, `service`, `model`, and `repository` packages.
- Ensure the code is clean, thread-safe (for in-memory storage), and error-handled.
- Include a `.gitignore` excluding standard build artifacts (gradle/, build/, .idea/, etc.).

### 6. Documentation & Deliverables
Please generate the following documentation and demo files as required by the assignment:
- **`README.md`**: A comprehensive overview of the project, features, and architecture.
- **`HOWTORUN.md`**: Step-by-step instructions on how to build and run the application.
- **`demo/run.bat`**: A Windows batch script to run the application.
- **`demo/sample-requests.http`**: A file with sample HTTP requests (JetBrains format) for testing all endpoints.
- **`demo/sample-data.json`**: A JSON file containing sample transaction data.
- **`docs/screenshots/`**: Mention that this folder should be created structure-wise.

### 7. Reference Testing Samples
Use the following samples (convert to `.http` format for `demo/sample-requests.http`) and adjust the port if necessary (Micronaut defaults to 8080):

```bash
# Create a transaction
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }'

# Get all transactions
curl http://localhost:8080/transactions

# Get transactions for specific account
curl "http://localhost:8080/transactions?accountId=ACC-12345"

# Get account balance
curl http://localhost:8080/accounts/ACC-12345/balance
```
