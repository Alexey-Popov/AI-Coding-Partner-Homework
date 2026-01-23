/**
 * Banking Transactions API
 * Main server file
 * Built with AI assistance (Claude Code)
 */

const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

// Import rate limiter
const { rateLimiter } = require('./utils/rateLimiter');

// Middleware to parse JSON request bodies
app.use(express.json());

// Apply rate limiting to all routes
app.use(rateLimiter);

// Basic health check endpoint
app.get('/', (req, res) => {
  res.json({
    message: 'Banking Transactions API',
    version: '1.0.0',
    status: 'running'
  });
});

// Import routes
const transactionRoutes = require('./routes/transactions');
const accountRoutes = require('./routes/accounts');

// Use routes
app.use('/transactions', transactionRoutes);
app.use('/accounts', accountRoutes);

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    error: 'Internal Server Error',
    message: err.message
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    error: 'Not Found',
    message: `Route ${req.method} ${req.url} not found`
  });
});

// Start server
const server = app.listen(PORT, () => {
  const transactionModel = require('./models/transaction');
  const transactionCount = transactionModel.getTransactionCount();

  console.log(`🚀 Banking Transactions API running on port ${PORT}`);
  console.log(`📡 Health check: http://localhost:${PORT}/`);
  console.log(`💾 Transactions in memory: ${transactionCount}`);
  console.log(`🔄 Server started: ${new Date().toISOString()}`);
});

// Graceful shutdown handler
function gracefulShutdown(signal) {
  console.log(`\n⚠️  Received ${signal}, shutting down gracefully...`);

  const transactionModel = require('./models/transaction');
  const transactionCount = transactionModel.getTransactionCount();

  console.log(`📊 Transactions in memory before cleanup: ${transactionCount}`);

  // Clear all transactions
  transactionModel.clearAllTransactions();

  console.log(`🧹 Transactions cleared: ${transactionModel.getTransactionCount()}`);
  console.log(`⏰ Shutdown time: ${new Date().toISOString()}`);

  // Close server
  server.close(() => {
    console.log(`✅ Server closed successfully`);
    process.exit(0);
  });

  // Force close after 10 seconds
  setTimeout(() => {
    console.error('❌ Forcing shutdown after timeout');
    process.exit(1);
  }, 10000);
}

// Handle shutdown signals
process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
process.on('SIGINT', () => gracefulShutdown('SIGINT'));

// Handle uncaught errors
process.on('uncaughtException', (error) => {
  console.error('💥 Uncaught Exception:', error);
  gracefulShutdown('UNCAUGHT_EXCEPTION');
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('💥 Unhandled Rejection at:', promise, 'reason:', reason);
  gracefulShutdown('UNHANDLED_REJECTION');
});

module.exports = app;
