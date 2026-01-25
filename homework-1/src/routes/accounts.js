const express = require('express');
const router = express.Router();
const { getAccountBalance } = require('../models/transaction');
const { isValidAccountId } = require('../validators/transactionValidator');
const { formatError, formatSuccess } = require('../utils/helpers');

/**
 * GET /accounts/:accountId/balance
 * Get the balance for a specific account
 */
router.get('/:accountId/balance', (req, res) => {
  const { accountId } = req.params;

  if (!isValidAccountId(accountId)) {
    return res.status(400).json(
      formatError('Invalid account ID format. Expected format: ACC-XXXXX (5 alphanumeric characters)')
    );
  }

  const balanceInfo = getAccountBalance(accountId);
  res.json(formatSuccess(balanceInfo));
});

module.exports = router;
