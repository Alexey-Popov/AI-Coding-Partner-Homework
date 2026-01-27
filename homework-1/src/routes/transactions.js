const express = require('express');
const Transaction = require('../models/transaction');
const { validateTransaction } = require('../validators/transactionValidator');
const { calculateBalance, filterTransactions, getAccountSummary } = require('../utils/helpers');

const router = express.Router();

// In-memory storage for transactions
let transactions = [];

/**
 * POST /transactions
 * Create a new transaction
 */
router.post('/', (req, res) => {
  try {
    const validationErrors = validateTransaction(req.body);

    if (validationErrors.length > 0) {
      return res.status(400).json({
        error: 'Validation failed',
        details: validationErrors
      });
    }

    const transaction = new Transaction(req.body);
    transactions.push(transaction);

    res.status(201).json(transaction);
  } catch (error) {
    res.status(500).json({ 
      error: 'Internal server error',
      message: error.message 
    });
  }
});

/**
 * GET /transactions
 * List all transactions with optional filtering
 */
router.get('/', (req, res) => {
  try {
    const { accountId, type, from, to } = req.query;
    
    const filters = { accountId, type, from, to };
    const filtered = filterTransactions(transactions, filters);

    res.status(200).json({
      count: filtered.length,
      transactions: filtered
    });
  } catch (error) {
    res.status(500).json({ 
      error: 'Internal server error',
      message: error.message 
    });
  }
});

/**
 * GET /transactions/:id
 * Get a specific transaction by ID
 */
router.get('/:id', (req, res) => {
  try {
    const transaction = transactions.find(t => t.id === req.params.id);

    if (!transaction) {
      return res.status(404).json({ 
        error: 'Transaction not found',
        message: `No transaction found with ID: ${req.params.id}`
      });
    }

    res.status(200).json(transaction);
  } catch (error) {
    res.status(500).json({ 
      error: 'Internal server error',
      message: error.message 
    });
  }
});

module.exports = router;
module.exports.transactions = transactions;
