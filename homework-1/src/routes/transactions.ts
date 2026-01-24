import { Router, Request, Response } from 'express';
import { CreateTransactionDTO, TransactionQueryParams } from '../models/transaction';
import { ErrorResponse } from '../models/common';
import { validateQueryParameters } from '../validators/queryValidator';
import { 
  getFilteredTransactions, 
  createTransaction, 
  getTransactionById,
  exportTransactionsAsCSV
} from '../controllers/transactionController';

const router = Router();

/**
 * POST /transactions
 * Create a new transaction
 */
router.post('/', (req: Request, res: Response) => {
  const dto: CreateTransactionDTO = req.body;

  // Create transaction (validation happens in controller)
  const result = createTransaction(dto);
  
  // Check if result is an error response
  if ('error' in result) {
    res.status(400).json(result);
    return;
  }

  res.status(201).json(result);
});

/**
 * GET /transactions
 * Get all transactions with optional filtering
 * Query params: accountId, type, from, to
 */
router.get('/', (req: Request, res: Response) => {
  const { accountId, type, from, to } = req.query as TransactionQueryParams;

  // Validate query parameters
  const validationErrors = validateQueryParameters({ accountId, type, from, to });
  if (validationErrors.length > 0) {
    const errorResponse: ErrorResponse = {
      error: 'Invalid query parameters',
      details: validationErrors
    };
    res.status(400).json(errorResponse);
    return;
  }

  // Get filtered transactions using controller
  const filteredTransactions = getFilteredTransactions({ accountId, type, from, to });

  res.status(200).json(filteredTransactions);
});

/**
 * GET /transactions/export
 * Export transactions as CSV with optional filtering
 * Query params: format (required: csv), accountId, type, from, to
 */
router.get('/export', (req: Request, res: Response) => {
  const { format, accountId, type, from, to } = req.query as TransactionQueryParams & { format?: string };

  // Validate format parameter
  if (!format || format.toLowerCase() !== 'csv') {
    const errorResponse: ErrorResponse = {
      error: 'Invalid format parameter',
      details: 'The format parameter must be set to "csv"'
    };
    res.status(400).json(errorResponse);
    return;
  }

  // Validate query parameters (same as regular GET)
  const validationErrors = validateQueryParameters({ accountId, type, from, to });
  if (validationErrors.length > 0) {
    const errorResponse: ErrorResponse = {
      error: 'Invalid query parameters',
      details: validationErrors
    };
    res.status(400).json(errorResponse);
    return;
  }

  // Export transactions as CSV using controller
  const { csv, filename } = exportTransactionsAsCSV({ accountId, type, from, to });

  // Set CSV response headers
  res.setHeader('Content-Type', 'text/csv; charset=utf-8');
  res.setHeader('Content-Disposition', `attachment; filename="${filename}"`);
  
  res.status(200).send(csv);
});

/**
 * GET /transactions/:id
 * Get a specific transaction by ID
 */
router.get('/:id', (req: Request, res: Response) => {
  const id = req.params.id as string;
  const transaction = getTransactionById(id);

  if (!transaction) {
    const errorResponse: ErrorResponse = {
      error: 'Transaction not found',
      details: { id }
    };
    res.status(404).json(errorResponse);
    return;
  }

  res.status(200).json(transaction);
});

export default router;
