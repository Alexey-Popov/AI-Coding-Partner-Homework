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

// Root endpoint
app.get('/', (req, res) => {
  res.json({
    message: 'Banking Transactions API',
    version: '1.0.0',
    endpoints: {
      transactions: {
        'POST /transactions': 'Create a new transaction',
        'GET /transactions': 'List all transactions (supports filtering)',
        'GET /transactions/:id': 'Get a specific transaction by ID'
      },
      accounts: {
        'GET /accounts/:accountId/balance': 'Get account balance',
        'GET /accounts/:accountId/summary': 'Get account summary with statistics'
      }
    }
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    error: 'Not found',
    message: `Route ${req.method} ${req.path} not found`
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    error: 'Internal server error',
    message: err.message
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 Banking Transactions API running on http://localhost:${PORT}`);
  console.log(`📝 API documentation available at http://localhost:${PORT}`);
});

module.exports = app;
