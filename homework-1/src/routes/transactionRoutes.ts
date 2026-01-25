import { Router } from 'express';
import { transactionController } from '../controllers/transactionController';

const router = Router();

/**
 * Transaction routes
 */

// Export must come before :id to avoid route conflict
router.get('/export', (req, res) => transactionController.exportTransactions(req, res));

// Create a new transaction
router.post('/', (req, res) => transactionController.createTransaction(req, res));

// Get all transactions (with optional filtering)
router.get('/', (req, res) => transactionController.getAllTransactions(req, res));

// Get a specific transaction by ID
router.get('/:id', (req, res) => transactionController.getTransactionById(req, res));

export default router;

