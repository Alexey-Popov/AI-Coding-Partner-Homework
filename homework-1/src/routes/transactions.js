/**
 * Transaction Routes
 * Handles all transaction-related API endpoints
 * Built with AI assistance (Claude Code)
 */

const express = require('express');
const router = express.Router();
const transactionModel = require('../models/transaction');
const validator = require('../validators/transactionValidator');

/**
 * POST /transactions
 * Create a new transaction
 *
 * Request body:
 * {
 *   "fromAccount": "ACC-12345",
 *   "toAccount": "ACC-67890",
 *   "amount": 100.50,
 *   "currency": "USD",
 *   "type": "transfer"
 * }
 */
router.post('/', (req, res) => {
  try {
    // Validate transaction data
    const errors = validator.validateTransaction(req.body);

    if (errors.length > 0) {
      return res.status(400).json(
        validator.createValidationErrorResponse(errors)
      );
    }

    // Create transaction
    const transaction = transactionModel.createTransaction(req.body);

    // Return created transaction with 201 status
    res.status(201).json(transaction);
  } catch (error) {
    console.error('Error creating transaction:', error);
    res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to create transaction'
    });
  }
});

/**
 * GET /transactions
 * List all transactions (with optional filtering)
 *
 * Query parameters:
 * - accountId: Filter by account (transactions involving this account)
 * - type: Filter by transaction type (deposit, withdrawal, transfer)
 * - from: Filter by start date (ISO 8601)
 * - to: Filter by end date (ISO 8601)
 */
router.get('/', (req, res) => {
  try {
    let transactions = transactionModel.getAllTransactions();

    // Apply filters if provided
    const { accountId, type, from, to } = req.query;

    // Filter by account
    if (accountId) {
      transactions = transactions.filter(t =>
        t.fromAccount === accountId || t.toAccount === accountId
      );
    }

    // Filter by type
    if (type) {
      transactions = transactions.filter(t =>
        t.type.toLowerCase() === type.toLowerCase()
      );
    }

    // Filter by date range
    if (from || to) {
      transactions = transactions.filter(t => {
        const transactionDate = new Date(t.timestamp);
        const startDate = from ? new Date(from) : new Date(0);
        const endDate = to ? new Date(to) : new Date();
        return transactionDate >= startDate && transactionDate <= endDate;
      });
    }

    res.json(transactions);
  } catch (error) {
    console.error('Error getting transactions:', error);
    res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to retrieve transactions'
    });
  }
});

/**
 * GET /transactions/:id
 * Get a specific transaction by ID
 */
router.get('/:id', (req, res) => {
  try {
    const { id } = req.params;
    const transaction = transactionModel.getTransactionById(id);

    if (!transaction) {
      return res.status(404).json({
        error: 'Transaction not found',
        id: id
      });
    }

    res.json(transaction);
  } catch (error) {
    console.error('Error getting transaction:', error);
    res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to retrieve transaction'
    });
  }
});

module.exports = router;
