const express = require('express');
const transactionRoutes = require('./routes');
const accountRoutes = require('./accountRoutes');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(express.json());

// Routes
app.use('/transactions', transactionRoutes);
app.use('/accounts', accountRoutes);

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok', message: 'Banking API is running' });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    error: 'Not found',
    path: req.path,
    method: req.method
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({
    error: 'Internal server error',
    message: err.message
  });
});

// Start server only if not in test mode
if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`🏦 Banking Transactions API running on http://localhost:${PORT}`);
    console.log(`📝 API Documentation:`);
    console.log(`   - POST /transactions - Create a transaction`);
    console.log(`   - GET /transactions - List all transactions`);
    console.log(`   - GET /transactions/:id - Get a transaction`);
    console.log(`   - GET /accounts/:accountId/balance - Get account balance`);
    console.log(`   - GET /accounts/:accountId/summary - Get account summary`);
    console.log(`   - GET /transactions/export?format=csv - Export as CSV`);
    console.log(`   - GET /health - Health check`);
  });
}

module.exports = app;
