import express, { Application, Request, Response, NextFunction } from 'express';
import dotenv from 'dotenv';
import transactionRoutes from './routes/transactionRoutes';
import accountRoutes from './routes/accountRoutes';

// Load environment variables
dotenv.config();

const app: Application = express();
const PORT = process.env.PORT || 3000;

/**
 * Middleware Configuration
 */

// Parse JSON request bodies
app.use(express.json());

// Parse URL-encoded request bodies
app.use(express.urlencoded({ extended: true }));

// Request logging middleware
app.use((req: Request, _res: Response, next: NextFunction) => {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] ${req.method} ${req.path}`);
  next();
});

/**
 * Routes
 */

// Health check endpoint
app.get('/', (_req: Request, res: Response) => {
  res.json({
    message: 'Banking Transactions API',
    version: '1.0.0',
    status: 'running',
    endpoints: {
      transactions: '/transactions',
      accounts: '/accounts'
    }
  });
});

// API routes
app.use('/transactions', transactionRoutes);
app.use('/accounts', accountRoutes);

/**
 * Error Handling Middleware
 */

// 404 handler - must be after all routes
app.use((req: Request, res: Response) => {
  res.status(404).json({
    error: 'Not Found',
    details: [
      {
        field: 'path',
        message: `Route ${req.method} ${req.path} not found`
      }
    ]
  });
});

// Global error handler
app.use((err: Error, _req: Request, res: Response, _next: NextFunction) => {
  console.error('Unhandled error:', err);
  res.status(500).json({
    error: 'Internal Server Error',
    details: [
      {
        field: 'server',
        message: 'An unexpected error occurred'
      }
    ]
  });
});

/**
 * Start Server
 */
app.listen(PORT, () => {
  console.log('='.repeat(50));
  console.log('🏦 Banking Transactions API');
  console.log('='.repeat(50));
  console.log(`✅ Server running on http://localhost:${PORT}`);
  console.log(`📝 Environment: ${process.env.NODE_ENV || 'development'}`);
  console.log('='.repeat(50));
  console.log('\nAvailable endpoints:');
  console.log(`  GET    http://localhost:${PORT}/`);
  console.log(`  POST   http://localhost:${PORT}/transactions`);
  console.log(`  GET    http://localhost:${PORT}/transactions`);
  console.log(`  GET    http://localhost:${PORT}/transactions/:id`);
  console.log(`  GET    http://localhost:${PORT}/transactions/export?format=csv`);
  console.log(`  GET    http://localhost:${PORT}/accounts/:accountId/balance`);
  console.log('='.repeat(50));
});

export default app;

