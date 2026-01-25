import { Request, Response } from 'express';
import { accountService } from '../services/accountService';

/**
 * Account controller handling HTTP requests
 */
export class AccountController {
  /**
   * GET /accounts/:accountId/balance - Get account balance
   */
  getAccountBalance(req: Request, res: Response): void {
    try {
      const { accountId } = req.params;

      const balanceInfo = accountService.getAccountBalance(accountId);

      if (!balanceInfo) {
        res.status(404).json({
          error: 'Account not found',
          details: [{ 
            field: 'accountId', 
            message: `Account ${accountId} not found or has no transactions` 
          }]
        });
        return;
      }

      res.status(200).json({
        accountId,
        balance: balanceInfo.balance,
        currency: balanceInfo.currency
      });
    } catch (error) {
      console.error('Error getting account balance:', error);
      res.status(500).json({
        error: 'Internal server error',
        details: [{ field: 'server', message: 'An unexpected error occurred' }]
      });
    }
  }
}

export const accountController = new AccountController();

