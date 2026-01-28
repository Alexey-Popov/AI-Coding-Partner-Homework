# 🏦 Homework 1: Banking Transactions API

> **Student Name**: [Kostia Chaikivskyi]
> **Date Submitted**: [24.01.2026]
> **AI Tools Used**: [Claude Code, GitHub Copilot]

---

## 📋 Project Overview

A RESTful Banking Transactions API built with Node.js and Express.js that provides comprehensive transaction management capabilities. The API allows users to create, retrieve, and analyze financial transactions across multiple accounts.

### Key Features

- **Transaction Management**
  - Create deposits, withdrawals, and transfers
  - Query transactions with filters (account, type, date range)
  - Export transactions to CSV format

- **Account Analytics**
  - Real-time balance calculation
  - Account summary with deposit/withdrawal totals and transaction count
  - Simple interest calculation based on current balance

- **Security & Performance**
  - Rate limiting (100 requests/minute per IP)
  - Input validation on all endpoints
  - Proper error handling with descriptive messages

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/transactions` | POST | Create a new transaction |
| `/transactions` | GET | List transactions with optional filters |
| `/transactions/:id` | GET | Get a specific transaction |
| `/transactions/export` | GET | Export transactions as CSV |
| `/accounts/:id/balance` | GET | Get account balance |
| `/accounts/:id/summary` | GET | Get account summary statistics |
| `/accounts/:id/interest` | GET | Calculate simple interest |

### Tech Stack

- **Runtime:** Node.js
- **Framework:** Express.js
- **Data Storage:** In-memory (array-based)

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

</div>
