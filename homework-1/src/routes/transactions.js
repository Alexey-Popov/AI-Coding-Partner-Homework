const express = require('express');
const router = express.Router();
const {
  createTransaction,
  getTransactions,
  getTransactionById
} = require('../models/transaction');
const { validateTransaction } = require('../validators/transactionValidator');
const { toCSV, formatError, formatSuccess } = require('../utils/helpers');

/**
 * POST /transactions
 * Create a new transaction
 */
router.post('/', (req, res) => {
  const validation = validateTransaction(req.body);

  if (!validation.isValid) {
    return res.status(400).json({
      error: 'Validation failed',
      details: validation.errors
    });
  }

  const transaction = createTransaction({
    fromAccount: req.body.fromAccount || null,
    toAccount: req.body.toAccount || null,
    amount: req.body.amount,
    currency: req.body.currency.toUpperCase(),
    type: req.body.type,
    status: req.body.status || 'completed'
  });

  res.status(201).json(formatSuccess(transaction, 'Transaction created successfully'));
});

/**
 * GET /transactions
 * List all transactions with optional filtering
 * Query params: accountId, type, from, to
 */
router.get('/', (req, res) => {
  const filters = {
    accountId: req.query.accountId,
    type: req.query.type,
    from: req.query.from,
    to: req.query.to
  };

  const transactions = getTransactions(filters);
  res.json(formatSuccess(transactions));
});

/**
 * GET /transactions/export
 * Export transactions as CSV
 * Query params: format (csv), accountId, type, from, to
 */
router.get('/export', (req, res) => {
  const format = req.query.format?.toLowerCase();

  if (format !== 'csv') {
    return res.status(400).json(formatError('Invalid format. Supported formats: csv'));
  }

  const filters = {
    accountId: req.query.accountId,
    type: req.query.type,
    from: req.query.from,
    to: req.query.to
  };

  const transactions = getTransactions(filters);

  if (transactions.length === 0) {
    return res.status(200).send('');
  }

  const csv = toCSV(transactions);

  res.setHeader('Content-Type', 'text/csv');
  res.setHeader('Content-Disposition', 'attachment; filename="transactions.csv"');
  res.send(csv);
});

/**
 * GET /transactions/:id
 * Get a specific transaction by ID
 */
router.get('/:id', (req, res) => {
  const transaction = getTransactionById(req.params.id);

  if (!transaction) {
    return res.status(404).json(formatError('Transaction not found'));
  }

  res.json(formatSuccess(transaction));
});

module.exports = router;
