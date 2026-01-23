/**
 * Account routes for banking API
 */

const express = require('express');
const router = express.Router();
const Transaction = require('../models/Transaction');
const { validateAccountFormat } = require('../validators/validators');
const { successResponse, errorResponse } = require('../utils/response');

/**
 * GET /accounts/:accountId/balance - Get account balance
 * @route GET /accounts/:accountId/balance
 * @param {string} accountId - Account ID (ACC-XXXXX format)
 * @returns {Object} 200 - Account balance information
 * @returns {Object} 400 - Validation error
 * @returns {Object} 500 - Server error
 */
router.get('/:accountId/balance', (req, res) => {
  try {
    const { accountId } = req.params;

    // Validate account format
    const validation = validateAccountFormat(accountId);
    if (!validation.valid) {
      return errorResponse(res, validation.error, 400);
    }

    const balance = Transaction.getAccountBalance(accountId);

    successResponse(res, {
      accountId,
      balance
    });
  } catch (error) {
    errorResponse(res, 'Failed to retrieve balance', 500);
  }
});

module.exports = router;
