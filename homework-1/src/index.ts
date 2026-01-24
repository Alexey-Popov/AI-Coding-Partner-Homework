import express, { Request, Response, NextFunction } from 'express';
import rateLimit from 'express-rate-limit';
import transactionsRouter from './routes/transactions';
import accountsRouter from './routes/accounts';
import { ErrorResponse } from './models/common';

const app = express();
const PORT = 3000;

// Rate limiting: 100 requests per minute per IP
const limiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 100, // Limit each IP to 100 requests per windowMs
  message: {
    error: 'Too many requests',
    details: 'You have exceeded the 100 requests per minute limit. Please try again later.'
  },
  standardHeaders: true, // Return rate limit info in the `RateLimit-*` headers
  legacyHeaders: false, // Disable the `X-RateLimit-*` headers
});

// Middleware
app.use(limiter);
app.use(express.json());

// Routes
app.use('/transactions', transactionsRouter);
app.use('/accounts', accountsRouter);

// Root endpoint
app.get('/', (_req: Request, res: Response) => {
  res.json({
    message: 'Banking Transactions API',
    version: '1.0.0',
    endpoints: {
      transactions: {
        'POST /transactions': 'Create a new transaction',
        'GET /transactions': 'Get all transactions (supports filters: accountId, type, from, to)',
        'GET /transactions/:id': 'Get a transaction by ID',
        'GET /transactions/export?format=csv': 'Export transactions as CSV (supports same filters)'
      },
      accounts: {
        'GET /accounts/:accountId/balance': 'Get account balance',
        'GET /accounts/:accountId/summary': 'Get account transaction summary'
      }
    }
  });
});

// Error handling middleware
app.use((err: Error, _req: Request, res: Response, _next: NextFunction) => {
  console.error(err.stack);
  
  const errorResponse: ErrorResponse = {
    error: 'Internal server error',
    details: err.message
  };
  
  res.status(500).json(errorResponse);
});

// 404 handler
app.use((req: Request, res: Response) => {
  const errorResponse: ErrorResponse = {
    error: 'Not found',
    details: `Route ${req.method} ${req.path} not found`
  };
  res.status(404).json(errorResponse);
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 Banking Transactions API running on http://localhost:${PORT}`);
  console.log(`📝 API Documentation available at http://localhost:${PORT}/`);
});
