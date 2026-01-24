import { Router, Request, Response } from 'express';
import * as accountService from '../services/account.service';
import { formatErrorResponse } from '../errors';

const router = Router();

router.get('/:accountId/balance', (req: Request, res: Response): void => {
  try {
    const balance = accountService.getAccountBalance(req.params.accountId as string);
    res.status(200).json(balance);
  } catch (error) {
    const { statusCode, body } = formatErrorResponse(error);
    res.status(statusCode).json(body);
  }
});

export default router;
