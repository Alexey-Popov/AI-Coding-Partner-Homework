/**
 * Account Routes
 * Handles account-related API endpoints
 * Built with AI assistance (Claude Code)
 */

const express = require('express');
const router = express.Router();
const transactionModel = require('../models/transaction');

/**
 * GET /accounts/:accountId/balance
 * Get the current balance for an account
 *
 * Calculates balance based on all transactions:
 * - Deposits and incoming transfers: added to balance
 * - Withdrawals and outgoing transfers: subtracted from balance
 */
router.get('/:accountId/balance', (req, res) => {
  try {
    const { accountId } = req.params;

    // Calculate balance
    const balanceInfo = transactionModel.calculateBalance(accountId);

    res.json(balanceInfo);
  } catch (error) {
    console.error('Error calculating balance:', error);
    res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to calculate account balance'
    });
  }
});

/**
 * GET /accounts/:accountId/summary
 * Get transaction summary for an account
 * (Task 4: Additional feature - optional for now)
 */
router.get('/:accountId/summary', (req, res) => {
  try {
    const { accountId } = req.params;
    const transactions = transactionModel.getTransactionsByAccount(accountId);

    if (transactions.length === 0) {
      return res.status(404).json({
        error: 'No transactions found',
        accountId: accountId
      });
    }

    // Calculate summary
    let totalDeposits = 0;
    let totalWithdrawals = 0;
    let mostRecentTransaction = null;

    transactions.forEach(t => {
      // Count deposits and incoming transfers
      if (t.toAccount === accountId) {
        totalDeposits += t.amount;
      }

      // Count withdrawals and outgoing transfers
      if (t.fromAccount === accountId) {
        totalWithdrawals += t.amount;
      }

      // Track most recent transaction
      if (!mostRecentTransaction ||
          new Date(t.timestamp) > new Date(mostRecentTransaction)) {
        mostRecentTransaction = t.timestamp;
      }
    });

    const summary = {
      accountId,
      totalDeposits: Math.round(totalDeposits * 100) / 100,
      totalWithdrawals: Math.round(totalWithdrawals * 100) / 100,
      netAmount: Math.round((totalDeposits - totalWithdrawals) * 100) / 100,
      transactionCount: transactions.length,
      mostRecentTransaction
    };

    res.json(summary);
  } catch (error) {
    console.error('Error getting account summary:', error);
    res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to retrieve account summary'
    });
  }
});

module.exports = router;
