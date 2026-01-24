import { Router, Request, Response } from 'express';
import { calculateAccountBalance, getAccountSummary } from '../controllers/accountController';

const router = Router();

/**
 * GET /accounts/:accountId/balance
 * Calculate and return the balance for an account
 */
router.get('/:accountId/balance', (req: Request, res: Response) => {
  const accountId = req.params.accountId as string;
  const balanceResponse = calculateAccountBalance(accountId);
  res.status(200).json(balanceResponse);
});

/**
 * GET /accounts/:accountId/summary
 * Get transaction summary for an account
 */
router.get('/:accountId/summary', (req: Request, res: Response) => {
  const accountId = req.params.accountId as string;
  const summary = getAccountSummary(accountId);
  res.status(200).json(summary);
});

export default router;
