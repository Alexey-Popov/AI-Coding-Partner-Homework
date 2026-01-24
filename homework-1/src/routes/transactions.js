const express = require('express');
const router = express.Router();
const storage = require('../utils/storage');

/**
 * POST /transactions - Create a new transaction
 */
router.post('/', (req, res) => {
  try {
    const { fromAccount, toAccount, amount, currency, type } = req.body;
    
    // Basic validation
    if (!amount || typeof amount !== 'number' || amount <= 0) {
      return res.status(400).json({ 
        error: 'Validation failed',
        details: [{ field: 'amount', message: 'Amount must be a positive number' }]
      });
    }
    
    if (!currency || typeof currency !== 'string') {
      return res.status(400).json({ 
        error: 'Validation failed',
        details: [{ field: 'currency', message: 'Currency is required' }]
      });
    }
    
    if (!type || !['deposit', 'withdrawal', 'transfer'].includes(type)) {
      return res.status(400).json({ 
        error: 'Validation failed',
        details: [{ field: 'type', message: 'Type must be deposit, withdrawal, or transfer' }]
      });
    }
    
    // Validate accounts based on type
    if (type === 'deposit' && !toAccount) {
      return res.status(400).json({ 
        error: 'Validation failed',
        details: [{ field: 'toAccount', message: 'toAccount is required for deposits' }]
      });
    }
    
    if (type === 'withdrawal' && !fromAccount) {
      return res.status(400).json({ 
        error: 'Validation failed',
        details: [{ field: 'fromAccount', message: 'fromAccount is required for withdrawals' }]
      });
    }
    
    if (type === 'transfer' && (!fromAccount || !toAccount)) {
      return res.status(400).json({ 
        error: 'Validation failed',
        details: [{ field: 'accounts', message: 'Both fromAccount and toAccount are required for transfers' }]
      });
    }
    
    const transaction = storage.createTransaction({
      fromAccount,
      toAccount,
      amount,
      currency,
      type
    });
    
    res.status(201).json(transaction);
  } catch (error) {
    res.status(500).json({ error: 'Internal server error', message: error.message });
  }
});

/**
 * GET /transactions - Get all transactions
 */
router.get('/', (req, res) => {
  try {
    const transactions = storage.getAllTransactions();
    res.status(200).json(transactions);
  } catch (error) {
    res.status(500).json({ error: 'Internal server error', message: error.message });
  }
});

/**
 * GET /transactions/:id - Get a specific transaction by ID
 */
router.get('/:id', (req, res) => {
  try {
    const { id } = req.params;
    const transaction = storage.getTransactionById(id);
    
    if (!transaction) {
      return res.status(404).json({ error: 'Transaction not found' });
    }
    
    res.status(200).json(transaction);
  } catch (error) {
    res.status(500).json({ error: 'Internal server error', message: error.message });
  }
});

module.exports = router;
