import { Router, Request, Response } from 'express';
import * as storage from '../utils/storage';
import { CreateTransactionInput, TransactionType, ValidationError } from '../types';

const router = Router();

/**
 * POST /transactions - Create a new transaction
 */
router.post('/', (req: Request, res: Response): void => {
  try {
    const { fromAccount, toAccount, amount, currency, type, description } = req.body;
    
    // Validation errors array
    const errors: ValidationError[] = [];
    
    // Validate amount
    if (!amount || typeof amount !== 'number' || amount <= 0) {
      errors.push({ field: 'amount', message: 'Amount must be a positive number' });
    }
    
    // Validate currency
    if (!currency || typeof currency !== 'string') {
      errors.push({ field: 'currency', message: 'Currency is required' });
    }
    
    // Validate type
    const validTypes: TransactionType[] = ['deposit', 'withdrawal', 'transfer'];
    if (!type || !validTypes.includes(type)) {
      errors.push({ field: 'type', message: 'Type must be deposit, withdrawal, or transfer' });
    }
    
    // Validate accounts based on type
    if (type === 'deposit' && !toAccount) {
      errors.push({ field: 'toAccount', message: 'toAccount is required for deposits' });
    }
    
    if (type === 'withdrawal' && !fromAccount) {
      errors.push({ field: 'fromAccount', message: 'fromAccount is required for withdrawals' });
    }
    
    if (type === 'transfer' && (!fromAccount || !toAccount)) {
      errors.push({ field: 'accounts', message: 'Both fromAccount and toAccount are required for transfers' });
    }
    
    // Return validation errors if any
    if (errors.length > 0) {
      res.status(400).json({ error: 'Validation failed', details: errors });
      return;
    }
    
    // Create transaction input with proper types
    const transactionInput: CreateTransactionInput = {
      fromAccount,
      toAccount,
      amount,
      currency,
      type: type as TransactionType,
      description
    };
    
    const transaction = storage.createTransaction(transactionInput);
    res.status(201).json(transaction);
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Unknown error';
    res.status(500).json({ error: 'Internal server error', message });
  }
});

/**
 * GET /transactions - Get all transactions
 */
router.get('/', (_req: Request, res: Response): void => {
  try {
    const transactions = storage.getAllTransactions();
    res.status(200).json(transactions);
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Unknown error';
    res.status(500).json({ error: 'Internal server error', message });
  }
});

/**
 * GET /transactions/:id - Get a specific transaction by ID
 */
router.get('/:id', (req: Request, res: Response): void => {
  try {
    const id = req.params.id as string;
    const transaction = storage.getTransactionById(id);
    
    if (!transaction) {
      res.status(404).json({ error: 'Transaction not found' });
      return;
    }
    
    res.status(200).json(transaction);
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Unknown error';
    res.status(500).json({ error: 'Internal server error', message });
  }
});

export default router;
