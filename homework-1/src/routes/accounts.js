const express = require('express');
const router = express.Router();
const storage = require('../utils/storage');

/**
 * GET /accounts/:accountId/balance - Get account balance
 */
router.get('/:accountId/balance', (req, res) => {
  try {
    const { accountId } = req.params;
    const balance = storage.getAccountBalance(accountId);
    
    if (balance === null) {
      return res.status(404).json({ 
        error: 'Account not found',
        message: `Account ${accountId} does not exist`
      });
    }
    
    res.status(200).json({
      accountId,
      balance,
      currency: 'USD' // Default currency
    });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error', message: error.message });
  }
});

module.exports = router;
