import { Router } from 'express';
import { createTransaction, getTransactions, getTransactionById } from '../models/transaction.js';
import { validateTransaction } from '../validators/transactionValidator.js';
import { toCSV, formatError, formatSuccess } from '../utils/helpers.js';

const router = Router();

/**
 * POST /transactions
 * Create a new transaction
 */
router.post('/', (req, res) => {
  const { body } = req;
  const validation = validateTransaction(body);

  if (!validation.isValid) {
    return res.status(400).json({
      error: 'Validation failed',
      details: validation.errors
    });
  }

  const transaction = createTransaction({
    fromAccount: body.fromAccount ?? null,
    toAccount: body.toAccount ?? null,
    amount: body.amount,
    currency: body.currency.toUpperCase(),
    type: body.type,
    status: body.status ?? 'completed'
  });

  return res.status(201).json(formatSuccess(transaction, 'Transaction created successfully'));
});

/**
 * GET /transactions
 * List all transactions with optional filtering
 * Query params: accountId, type, from, to
 */
router.get('/', (req, res) => {
  const { accountId, type, from, to } = req.query;
  const transactions = getTransactions({ accountId, type, from, to });
  return res.json(formatSuccess(transactions));
});

/**
 * GET /transactions/export
 * Export transactions as CSV
 * Query params: format (csv), accountId, type, from, to
 */
router.get('/export', (req, res) => {
  const { format, accountId, type, from, to } = req.query;

  if (format?.toLowerCase() !== 'csv') {
    return res.status(400).json(formatError('Invalid format. Supported formats: csv'));
  }

  const transactions = getTransactions({ accountId, type, from, to });

  if (transactions.length === 0) {
    return res.status(200).send('');
  }

  const csv = toCSV(transactions);

  res.setHeader('Content-Type', 'text/csv');
  res.setHeader('Content-Disposition', 'attachment; filename="transactions.csv"');
  return res.send(csv);
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

  return res.json(formatSuccess(transaction));
});

export default router;
