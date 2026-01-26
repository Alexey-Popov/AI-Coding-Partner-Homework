const express = require('express');
const router = express.Router();
const {
  createTransaction,
  getTransactions,
  getTransactionById
} = require('../models/transaction');
const { validateTransaction } = require('../validators/transactionValidator');

/**
 * POST /transactions
 * Create a new transaction
 */
router.post('/', (req, res) => {
  const errors = validateTransaction(req.body);

  if (errors.length > 0) {
    return res.status(400).json({
      error: 'Validation failed',
      details: errors
    });
  }

  const transaction = createTransaction({
    fromAccount: req.body.fromAccount,
    toAccount: req.body.toAccount,
    amount: req.body.amount,
    currency: req.body.currency.toUpperCase(),
    type: req.body.type.toLowerCase()
  });

  res.status(201).json(transaction);
});

/**
 * GET /transactions
 * List all transactions with optional filters
 * Query params:
 *   - accountId: Filter by account (from or to)
 *   - type: Filter by transaction type
 *   - from: Filter by start date
 *   - to: Filter by end date
 */
router.get('/', (req, res) => {
  const filters = {};

  if (req.query.accountId) {
    filters.accountId = req.query.accountId;
  }

  if (req.query.type) {
    filters.type = req.query.type.toLowerCase();
  }

  if (req.query.from) {
    filters.from = req.query.from;
  }

  if (req.query.to) {
    filters.to = req.query.to;
  }

  const transactions = getTransactions(filters);
  res.json(transactions);
});

/**
 * GET /transactions/:id
 * Get a transaction by ID
 */
router.get('/:id', (req, res) => {
  const transaction = getTransactionById(req.params.id);

  if (!transaction) {
    return res.status(404).json({
      error: 'Transaction not found',
      id: req.params.id
    });
  }

  res.json(transaction);
});

module.exports = router;
