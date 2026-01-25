const express = require('express');
const transactionsRouter = require('./routes/transactions');
const accountsRouter = require('./routes/accounts');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(express.json());

// Request logging middleware
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
  next();
});

// Routes
app.use('/transactions', transactionsRouter);
app.use('/accounts', accountsRouter);

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Root endpoint
app.get('/', (req, res) => {
  res.json({
    message: 'Banking Transactions API',
    version: '1.0.0',
    endpoints: {
      transactions: {
        'POST /transactions': 'Create a new transaction',
        'GET /transactions': 'List all transactions (supports filtering)',
        'GET /transactions/:id': 'Get a specific transaction',
        'GET /transactions/export?format=csv': 'Export transactions as CSV'
      },
      accounts: {
        'GET /accounts/:accountId/balance': 'Get account balance'
      },
      health: {
        'GET /health': 'Health check endpoint'
      }
    }
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Not found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Error:', err.message);
  res.status(500).json({ error: 'Internal server error' });
});

// Start server only when run directly (not when required for testing)
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`Banking Transactions API running on http://localhost:${PORT}`);
    console.log('Available endpoints:');
    console.log('  POST   /transactions          - Create a transaction');
    console.log('  GET    /transactions          - List all transactions');
    console.log('  GET    /transactions/:id      - Get transaction by ID');
    console.log('  GET    /transactions/export   - Export as CSV');
    console.log('  GET    /accounts/:id/balance  - Get account balance');
    console.log('  GET    /health                - Health check');
  });
}

module.exports = app;
