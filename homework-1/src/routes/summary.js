/**
 * Transaction summary endpoint (Optional Feature A)
 */

const express = require('express');
const router = express.Router();
const Transaction = require('../models/Transaction');
const { validateAccountFormat } = require('../validators/validators');
const { successResponse, errorResponse } = require('../utils/response');

/**
 * GET /summary - Get transaction summary
 * @route GET /summary
 * @param {string} [accountId] - Optional query parameter to filter by account
 * @returns {Object} 200 - Transaction summary with statistics
 * @returns {Object} 400 - Validation error
 * @returns {Object} 500 - Server error
 * @note Summary combines all currencies; filter by currency manually if needed
 */
router.get('/', (req, res) => {
  try {
    const { accountId } = req.query;
    let transactions = Transaction.getAllTransactions();

    // If accountId filter is provided, validate and filter
    if (accountId) {
      const validation = validateAccountFormat(accountId);
      if (!validation.valid) {
        return errorResponse(res, validation.error, 400);
      }

      transactions = transactions.filter(
        t => t.fromAccount === accountId || t.toAccount === accountId
      );
    }

    // Calculate summary statistics grouped by currency
    const currencyStats = {};
    let transactionCount = transactions.length;
    let mostRecentTransaction = null;

    transactions.forEach(t => {
      // Initialize currency if not exists
      if (!currencyStats[t.currency]) {
        currencyStats[t.currency] = {
          totalDeposits: 0,
          totalWithdrawals: 0,
          totalTransfers: 0,
          count: 0
        };
      }

      const stats = currencyStats[t.currency];
      stats.count++;

      if (t.type === 'deposit') stats.totalDeposits += t.amount;
      if (t.type === 'withdrawal') stats.totalWithdrawals += t.amount;
      if (t.type === 'transfer') stats.totalTransfers += t.amount;

      if (!mostRecentTransaction || new Date(t.timestamp) > new Date(mostRecentTransaction.timestamp)) {
        mostRecentTransaction = t;
      }
    });

    const summary = {
      filter: accountId ? { accountId } : null,
      statistics: {
        transactionCount,
        byCurrency: currencyStats
      },
      mostRecentTransaction: mostRecentTransaction || null
    };

    successResponse(res, summary);
  } catch (error) {
    errorResponse(res, 'Failed to retrieve summary', 500);
  }
});

module.exports = router;
