const express = require('express');
const { validateAccountNumber } = require('../validators/transactionValidator');
const { calculateBalance, getAccountSummary } = require('../utils/helpers');

const router = express.Router();

// Reference to transactions from transactions route
let getTransactions = () => {
  return require('./transactions').transactions;
};

/**
 * GET /accounts/:accountId/balance
 * Get account balance
 */
router.get('/:accountId/balance', (req, res) => {
  try {
    const { accountId } = req.params;

    if (!validateAccountNumber(accountId)) {
      return res.status(400).json({
        error: 'Validation failed',
        details: [{ 
          field: 'accountId', 
          message: 'Account ID must follow format ACC-XXXXX (where X is alphanumeric)' 
        }]
      });
    }

    const transactions = getTransactions();
    const balance = calculateBalance(transactions, accountId);

    res.status(200).json({
      accountId,
      balance: Math.round(balance * 100) / 100,
      currency: 'USD', // Default currency
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    res.status(500).json({ 
      error: 'Internal server error',
      message: error.message 
    });
  }
});

/**
 * GET /accounts/:accountId/summary
 * Get account summary with transaction statistics
 */
router.get('/:accountId/summary', (req, res) => {
  try {
    const { accountId } = req.params;

    if (!validateAccountNumber(accountId)) {
      return res.status(400).json({
        error: 'Validation failed',
        details: [{ 
          field: 'accountId', 
          message: 'Account ID must follow format ACC-XXXXX (where X is alphanumeric)' 
        }]
      });
    }

    const transactions = getTransactions();
    const summary = getAccountSummary(transactions, accountId);
    const balance = calculateBalance(transactions, accountId);

    res.status(200).json({
      accountId,
      currentBalance: Math.round(balance * 100) / 100,
      ...summary
    });
  } catch (error) {
    res.status(500).json({ 
      error: 'Internal server error',
      message: error.message 
    });
  }
});

module.exports = router;
