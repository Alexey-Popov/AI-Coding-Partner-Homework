const express = require('express');
const router = express.Router();
const { isValidAccountNumber } = require('../validators/transactionValidator');
const { getAccountBalance } = require('../data/store');

/**
 * GET /accounts/:accountId/balance
 * Get account balance
 */
router.get('/:accountId/balance', (req, res) => {
  const { accountId } = req.params;

  // Validate account format
  if (!isValidAccountNumber(accountId)) {
    return res.status(400).json({
      error: 'Invalid account',
      message: 'Account ID must follow format ACC-XXXXX (X is alphanumeric)'
    });
  }

  const balance = getAccountBalance(accountId);

  res.json({
    accountId,
    balance,
    currency: 'USD' // Default currency for balance display
  });
});

module.exports = router;
