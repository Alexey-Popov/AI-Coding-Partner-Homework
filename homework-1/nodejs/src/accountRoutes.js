// Routes for account endpoints
const express = require('express');
const router = express.Router();
const {
  getAccountBalance,
  getAccountSummary
} = require('./transaction');

// GET /accounts/:accountId/balance - Get account balance
router.get('/:accountId/balance', (req, res) => {
  const { accountId } = req.params;
  const balance = getAccountBalance(accountId);
  res.status(200).json(balance);
});

// GET /accounts/:accountId/summary - Get account summary (Option A feature)
router.get('/:accountId/summary', (req, res) => {
  const { accountId } = req.params;
  const summary = getAccountSummary(accountId);
  res.status(200).json(summary);
});

module.exports = router;
