import { Request, Response } from 'express';
import { transactionService } from '../services/transactionService';
import { csvExporter } from '../utils/csvExporter';
import { TransactionType, TransactionFilters } from '../models/transaction';

/**
 * Transaction controller handling HTTP requests
 */
export class TransactionController {
  /**
   * POST /transactions - Create a new transaction
   */
  createTransaction(req: Request, res: Response): void {
    try {
      const result = transactionService.createTransaction(req.body);

      if (result.errors) {
        res.status(400).json(result.errors);
        return;
      }

      res.status(201).json(result.transaction);
    } catch (error) {
      console.error('Error creating transaction:', error);
      res.status(500).json({
        error: 'Internal server error',
        details: [{ field: 'server', message: 'An unexpected error occurred' }]
      });
    }
  }

  /**
   * GET /transactions - Get all transactions with optional filtering
   */
  getAllTransactions(req: Request, res: Response): void {
    try {
      const filters: TransactionFilters = {
        accountId: req.query.accountId as string,
        type: req.query.type as TransactionType,
        from: req.query.from as string,
        to: req.query.to as string
      };

      // Remove undefined values
      Object.keys(filters).forEach(key => {
        if (filters[key as keyof TransactionFilters] === undefined) {
          delete filters[key as keyof TransactionFilters];
        }
      });

      const transactions = transactionService.getTransactions(
        Object.keys(filters).length > 0 ? filters : undefined
      );

      res.status(200).json(transactions);
    } catch (error) {
      console.error('Error getting transactions:', error);
      res.status(500).json({
        error: 'Internal server error',
        details: [{ field: 'server', message: 'An unexpected error occurred' }]
      });
    }
  }

  /**
   * GET /transactions/:id - Get a specific transaction by ID
   */
  getTransactionById(req: Request, res: Response): void {
    try {
      const { id } = req.params;
      const transaction = transactionService.getTransactionById(id);

      if (!transaction) {
        res.status(404).json({
          error: 'Transaction not found',
          details: [{ field: 'id', message: `Transaction with ID ${id} not found` }]
        });
        return;
      }

      res.status(200).json(transaction);
    } catch (error) {
      console.error('Error getting transaction:', error);
      res.status(500).json({
        error: 'Internal server error',
        details: [{ field: 'server', message: 'An unexpected error occurred' }]
      });
    }
  }

  /**
   * GET /transactions/export - Export transactions as CSV
   */
  exportTransactions(req: Request, res: Response): void {
    try {
      const format = req.query.format as string;

      if (format && format.toLowerCase() !== 'csv') {
        res.status(400).json({
          error: 'Invalid format',
          details: [{ field: 'format', message: 'Only CSV format is supported' }]
        });
        return;
      }

      // Get all transactions (could add filtering here too)
      const transactions = transactionService.getTransactions();

      // Convert to CSV
      const csvContent = csvExporter.exportTransactionsToCSV(transactions);

      // Set headers for file download
      const filename = csvExporter.generateFilename();
      res.setHeader('Content-Type', 'text/csv');
      res.setHeader('Content-Disposition', `attachment; filename="${filename}"`);

      res.status(200).send(csvContent);
    } catch (error) {
      console.error('Error exporting transactions:', error);
      res.status(500).json({
        error: 'Internal server error',
        details: [{ field: 'server', message: 'An unexpected error occurred' }]
      });
    }
  }
}

export const transactionController = new TransactionController();

