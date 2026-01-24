import { Router, Request, Response } from 'express';
import * as storage from '../utils/storage';
import { AccountBalance } from '../types';

const router = Router();

/**
 * GET /accounts/:accountId/balance - Get account balance
 */
router.get('/:accountId/balance', (req: Request, res: Response): void => {
  try {
    const accountId = req.params.accountId as string;
    const balance = storage.getAccountBalance(accountId);
    
    if (balance === null) {
      res.status(404).json({ 
        error: 'Account not found',
        message: `Account ${accountId} does not exist`
      });
      return;
    }
    
    const response: AccountBalance = {
      accountId,
      balance,
      currency: 'USD' // Default currency
    };
    
    res.status(200).json(response);
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Unknown error';
    res.status(500).json({ error: 'Internal server error', message });
  }
});

export default router;
