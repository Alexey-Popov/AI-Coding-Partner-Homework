const express = require('express');
const router = express.Router();
const { validateTransaction } = require('../validators/transactionValidator');
const {
  getAllTransactions,
  getTransactionById,
  addTransaction,
  getFilteredTransactions
} = require('../data/store');

/**
 * GET /transactions/export
 * Export transactions as CSV
 * Must be defined before /:id to avoid route conflict
 */
router.get('/export', (req, res) => {
  const { format } = req.query;

  if (format !== 'csv') {
    return res.status(400).json({
      error: 'Invalid format',
      message: 'Only CSV format is supported. Use ?format=csv'
    });
  }

  const transactions = getAllTransactions();

  // CSV headers
  const headers = ['id', 'fromAccount', 'toAccount', 'amount', 'currency', 'type', 'timestamp', 'status'];
  const csvLines = [headers.join(',')];

  // CSV rows
  for (const t of transactions) {
    const row = headers.map(h => {
      const value = t[h];
      // Escape values that contain commas or quotes
      if (typeof value === 'string' && (value.includes(',') || value.includes('"'))) {
        return `"${value.replace(/"/g, '""')}"`;
      }
      return value;
    });
    csvLines.push(row.join(','));
  }

  const csv = csvLines.join('\n');

  res.setHeader('Content-Type', 'text/csv');
  res.setHeader('Content-Disposition', 'attachment; filename="transactions.csv"');
  res.send(csv);
});

/**
 * GET /transactions
 * List all transactions with optional filtering
 */
router.get('/', (req, res) => {
  const { accountId, type, from, to } = req.query;

  // If any filters are provided, use filtered query
  if (accountId || type || from || to) {
    const filtered = getFilteredTransactions({ accountId, type, from, to });
    return res.json(filtered);
  }

  // Return all transactions
  res.json(getAllTransactions());
});

/**
 * GET /transactions/:id
 * Get a specific transaction by ID
 */
router.get('/:id', (req, res) => {
  const transaction = getTransactionById(req.params.id);

  if (!transaction) {
    return res.status(404).json({
      error: 'Not found',
      message: `Transaction with ID ${req.params.id} not found`
    });
  }

  res.json(transaction);
});

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

  const transaction = addTransaction(req.body);
  res.status(201).json(transaction);
});

module.exports = router;
