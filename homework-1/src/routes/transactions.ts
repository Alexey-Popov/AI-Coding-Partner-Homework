import { Router, Request, Response } from 'express';
import * as transactionService from '../services/transaction.service';
import { validateTransactionInput } from '../validators/transaction.validator';
import { formatErrorResponse } from '../errors';
import { TransactionFilter, TransactionType } from '../models';

const router = Router();

router.post('/', (req: Request, res: Response): void => {
  try {
    const input = validateTransactionInput(req.body);
    const transaction = transactionService.createTransaction(input);
    res.status(201).json(transaction);
  } catch (error) {
    const { statusCode, body } = formatErrorResponse(error);
    res.status(statusCode).json(body);
  }
});

router.get('/', (req: Request, res: Response): void => {
  try {
    const filter: TransactionFilter = {};

    if (req.query.accountId) {
      filter.accountId = req.query.accountId as string;
    }
    if (req.query.type) {
      filter.type = req.query.type as TransactionType;
    }
    if (req.query.from) {
      filter.from = req.query.from as string;
    }
    if (req.query.to) {
      filter.to = req.query.to as string;
    }

    const transactions = transactionService.getAllTransactions(
      Object.keys(filter).length > 0 ? filter : undefined
    );
    res.status(200).json(transactions);
  } catch (error) {
    const { statusCode, body } = formatErrorResponse(error);
    res.status(statusCode).json(body);
  }
});

router.get('/:id', (req: Request, res: Response): void => {
  try {
    const transaction = transactionService.getTransactionById(req.params.id as string);
    res.status(200).json(transaction);
  } catch (error) {
    const { statusCode, body } = formatErrorResponse(error);
    res.status(statusCode).json(body);
  }
});

export default router;
