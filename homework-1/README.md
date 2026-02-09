# 🏦 Homework 1: Banking Transactions API

> **Student Name**: Ruslan Pistriak
> **Date Submitted**: 9 February 2026
> **AI Tools Used**: GitHub Copilot

---

## 📋 Project Overview

This project delivers a lightweight banking transactions service built with Java Spring Boot and backed by in‑memory data. It records deposits, withdrawals, and transfers, maintains per‑account balances derived from the transaction log, and offers a simple REST interface for interacting with the system. Requests are validated with clear feedback: amounts must be positive with up to two decimal places, account identifiers follow the `ACC-XXXXX` pattern, and only valid ISO 4217 currency codes are accepted. Clients can review transaction history with optional filtering by account, type, and time window, and they can quickly gauge activity through an account summary that aggregates totals and highlights the most recent transaction. The design favors clarity and ease of testing—no external database, straightforward models, and predictable responses.

As per the assignment, this project implements:
- Task 1 — Core API Implementation: REST endpoints to create, list, fetch transactions, and retrieve per-account balances using in-memory storage.
- Task 2 — Transaction Validation: Amount positivity (up to two decimals), `ACC-XXXXX` account format, and ISO 4217 currency codes with clear error responses.
- Task 3 — Basic Transaction History: Filtering by account, type, and date range on the transactions list endpoint.
- Task 4 (Option A) — Account Summary Endpoint: `/accounts/:accountId/summary` aggregates totals and highlights the most recent transaction.

For setup and run steps, see HOWTO RUN instructions in [homework-1/HOWTORUN.md](homework-1/HOWTORUN.md).

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

</div>
