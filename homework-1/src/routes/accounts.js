const express = require('express');
const router = express.Router();
const { getAccountBalance, accountExists } = require('../models/transaction');
const {
  validateAccountId,
  validateInterestParams
} = require('../validators/transactionValidator');
const { roundToTwoDecimals } = require('../utils/helpers');

/**
 * GET /accounts/:accountId/balance
 * Get the balance of an account
 */
router.get('/:accountId/balance', (req, res) => {
  const { accountId } = req.params;

  // Validate account ID format
  const validationError = validateAccountId(accountId);
  if (validationError) {
    return res.status(400).json({
      error: 'Validation failed',
      details: [{ field: 'accountId', message: validationError }]
    });
  }

  // Check if account exists
  if (!accountExists(accountId)) {
    return res.status(404).json({
      error: 'Account not found',
      accountId: accountId
    });
  }

  const balance = getAccountBalance(accountId);

  res.json({
    accountId: accountId,
    balance: roundToTwoDecimals(balance)
  });
});

/**
 * GET /accounts/:accountId/interest
 * Calculate simple interest on account balance
 * Query params:
 *   - rate: Interest rate (decimal, e.g., 0.05 for 5%)
 *   - days: Number of days to calculate interest for
 * Formula: interest = balance * rate * (days / 365)
 */
router.get('/:accountId/interest', (req, res) => {
  const { accountId } = req.params;
  const { rate, days } = req.query;

  // Validate account ID format
  const accountError = validateAccountId(accountId);
  if (accountError) {
    return res.status(400).json({
      error: 'Validation failed',
      details: [{ field: 'accountId', message: accountError }]
    });
  }

  // Validate interest parameters
  const paramErrors = validateInterestParams(rate, days);
  if (paramErrors.length > 0) {
    return res.status(400).json({
      error: 'Validation failed',
      details: paramErrors
    });
  }

  // Check if account exists
  if (!accountExists(accountId)) {
    return res.status(404).json({
      error: 'Account not found',
      accountId: accountId
    });
  }

  // Get current balance
  const balance = getAccountBalance(accountId);

  // Parse parameters with defaults
  const interestRate = rate !== undefined ? parseFloat(rate) : 0.05;
  const numDays = days !== undefined ? parseInt(days) : 30;

  // Calculate simple interest: interest = principal * rate * time
  const interestAmount = balance * interestRate * (numDays / 365);
  const totalWithInterest = balance + interestAmount;

  res.json({
    accountId: accountId,
    currentBalance: roundToTwoDecimals(balance),
    interestRate: interestRate,
    days: numDays,
    interestAmount: roundToTwoDecimals(interestAmount),
    totalWithInterest: roundToTwoDecimals(totalWithInterest)
  });
});

module.exports = router;
