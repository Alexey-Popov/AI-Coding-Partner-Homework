import { Router } from 'express';
import { getAccountBalance } from '../models/transaction.js';
import { isValidAccountId } from '../validators/transactionValidator.js';
import { formatError, formatSuccess } from '../utils/helpers.js';

const router = Router();

/**
 * GET /accounts/:accountId/balance
 * Get the balance for a specific account
 */
router.get('/:accountId/balance', (req, res) => {
  const { accountId } = req.params;

  if (!isValidAccountId(accountId)) {
    return res.status(400).json(
      formatError('Invalid account ID format. Expected format: ACC-XXXXX (5 alphanumeric characters)')
    );
  }

  const balanceInfo = getAccountBalance(accountId);
  return res.json(formatSuccess(balanceInfo));
});

export default router;
