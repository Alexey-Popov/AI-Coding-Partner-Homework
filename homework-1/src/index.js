const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Routes
const transactionsRouter = require('./routes/transactions');
const accountsRouter = require('./routes/accounts');

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
        'GET /transactions': 'List all transactions',
        'GET /transactions/:id': 'Get a specific transaction by ID'
      },
      accounts: {
        'GET /accounts/:accountId/balance': 'Get account balance'
      }
    }
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: 'Something went wrong!' });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Endpoint not found' });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 Banking Transactions API running on http://localhost:${PORT}`);
  console.log(`📝 API Documentation available at http://localhost:${PORT}/`);
});

module.exports = app;
