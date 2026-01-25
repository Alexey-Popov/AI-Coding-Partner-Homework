import express, { Express, Request, Response, NextFunction } from 'express';
import transactionsRouter from './routes/transactions';
import accountsRouter from './routes/accounts';
import { rateLimiter } from './middleware/rateLimiter';

const app: Express = express();

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(rateLimiter);

// Routes
app.use('/transactions', transactionsRouter);
app.use('/accounts', accountsRouter);

// Root endpoint
app.get('/', (_req: Request, res: Response): void => {
  res.json({
    message: 'Banking Transactions API',
    version: '1.0.0',
    endpoints: {
      transactions: {
        'POST /transactions': 'Create a new transaction',
        'GET /transactions': 'List all transactions',
        'GET /transactions/:id': 'Get a specific transaction by ID'
      },
      accounts: {
        'GET /accounts/:accountId/balance': 'Get account balance'
      }
    }
  });
});

// Test endpoint to trigger error middleware (for testing purposes)
app.get('/__test__/error', (_req: Request, _res: Response, next: NextFunction): void => {
  next(new Error('Test error'));
});

// Error handling middleware
app.use((err: Error, _req: Request, res: Response, _next: NextFunction): void => {
  console.error(err.stack);
  res.status(500).json({ error: 'Something went wrong!' });
});

// 404 handler
app.use((_req: Request, res: Response): void => {
  res.status(404).json({ error: 'Endpoint not found' });
});

export default app;
