/**
 * Transaction routes for banking API
 */

const express = require('express');
const router = express.Router();
const Transaction = require('../models/Transaction');
const { validateTransactionData } = require('../validators/validators');
const { successResponse, errorResponse } = require('../utils/response');

/**
 * POST /transactions - Create a new transaction
 */
router.post('/', (req, res) => {
  const { fromAccount, toAccount, amount, currency, type } = req.body;

  // Validate input data
  const validation = validateTransactionData({
    fromAccount,
    toAccount,
    amount,
    currency,
    type
  });

  if (!validation.valid) {
    return errorResponse(res, 'Validation failed', 400, validation.errors);
  }

  try {
    const transaction = Transaction.createTransaction({
      fromAccount,
      toAccount,
      amount,
      currency,
      type
    });

    successResponse(res, transaction, 201, 'Transaction created successfully');
  } catch (error) {
    errorResponse(res, 'Failed to create transaction', 500);
  }
});

/**
 * GET /transactions - List all transactions with optional filters
 */
router.get('/', (req, res) => {
  try {
    const filters = {};
    const { validateDateFormat } = require('../validators/validators');

    // Extract and validate filter parameters
    if (req.query.accountId) {
      filters.accountId = req.query.accountId;
    }
    
    if (req.query.type) {
      const validTypes = ['deposit', 'withdrawal', 'transfer'];
      if (!validTypes.includes(req.query.type)) {
        return errorResponse(res, `Invalid type. Must be one of: ${validTypes.join(', ')}`, 400);
      }
      filters.type = req.query.type;
    }
    
    if (req.query.from) {
      const dateValidation = validateDateFormat(req.query.from);
      if (!dateValidation.valid) {
        return errorResponse(res, `Invalid 'from' date: ${dateValidation.error}`, 400);
      }
      filters.from = req.query.from;
    }
    
    if (req.query.to) {
      const dateValidation = validateDateFormat(req.query.to);
      if (!dateValidation.valid) {
        return errorResponse(res, `Invalid 'to' date: ${dateValidation.error}`, 400);
      }
      filters.to = req.query.to;
    }

    const transactions = Transaction.getTransactionsFiltered(filters);
    successResponse(res, {
      count: transactions.length,
      transactions
    });
  } catch (error) {
    console.error('Error retrieving transactions:', error);
    errorResponse(res, 'Failed to retrieve transactions', 500);
  }
});

/**
 * GET /transactions/:id - Get specific transaction by ID
 */
router.get('/:id', (req, res) => {
  try {
    const transaction = Transaction.getTransactionById(req.params.id);

    if (!transaction) {
      return errorResponse(res, 'Transaction not found', 404);
    }

    successResponse(res, transaction);
  } catch (error) {
    errorResponse(res, 'Failed to retrieve transaction', 500);
  }
});

module.exports = router;
