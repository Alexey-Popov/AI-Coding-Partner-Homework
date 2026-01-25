// Routes for transaction and account endpoints
const express = require('express');
const router = express.Router();
const { validateTransaction } = require('./validators');
const {
  createTransaction,
  getTransactions,
  getTransactionById,
  getAccountBalance,
  getAccountSummary,
  transactions
} = require('./transaction');

// ===== TRANSACTION ROUTES =====

// POST /transactions - Create a new transaction
router.post('/', (req, res) => {
  const { fromAccount, toAccount, amount, currency, type } = req.body;

  const validation = validateTransaction({
    fromAccount,
    toAccount,
    amount,
    currency,
    type
  });

  if (!validation.isValid) {
    return res.status(400).json({
      error: 'Validation failed',
      details: validation.errors
    });
  }

  const transaction = createTransaction({
    fromAccount,
    toAccount,
    amount: parseFloat(amount),
    currency: currency.toUpperCase(),
    type
  });

  res.status(201).json(transaction);
});

// GET /transactions - List all transactions with filtering
router.get('/', (req, res) => {
  const { accountId, type, from, to } = req.query;

  const filters = {};
  if (accountId) filters.accountId = accountId;
  if (type) filters.type = type;
  if (from) filters.from = from;
  if (to) filters.to = to;

  const result = getTransactions(filters);
  res.status(200).json(result);
});

// GET /transactions/export - Export transactions as CSV (Option C feature)
// NOTE: This route must be defined before /:id to avoid matching "export" as an ID
router.get('/export', (req, res) => {
  const { format } = req.query;

  if (format !== 'csv') {
    return res.status(400).json({
      error: 'Invalid format. Use format=csv'
    });
  }

  // Generate CSV
  const headers = ['ID', 'From Account', 'To Account', 'Amount', 'Currency', 'Type', 'Timestamp', 'Status'];
  const rows = transactions.map(t => [
    t.id,
    t.fromAccount,
    t.toAccount,
    t.amount,
    t.currency,
    t.type,
    t.timestamp,
    t.status
  ]);

  const csv = [
    headers.join(','),
    ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
  ].join('\n');

  res.setHeader('Content-Type', 'text/csv');
  res.setHeader('Content-Disposition', 'attachment; filename="transactions.csv"');
  res.send(csv);
});

// GET /transactions/:id - Get a specific transaction by ID
router.get('/:id', (req, res) => {
  const { id } = req.params;
  const transaction = getTransactionById(id);

  if (!transaction) {
    return res.status(404).json({
      error: 'Transaction not found',
      id
    });
  }

  res.status(200).json(transaction);
});

module.exports = router;
