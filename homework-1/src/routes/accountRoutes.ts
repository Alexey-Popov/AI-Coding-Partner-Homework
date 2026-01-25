import { Router } from 'express';
import { accountController } from '../controllers/accountController';

const router = Router();

/**
 * Account routes
 */

// Get account balance
router.get('/:accountId/balance', (req, res) => accountController.getAccountBalance(req, res));

export default router;

