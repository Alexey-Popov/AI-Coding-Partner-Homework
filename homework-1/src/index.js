/**
 * Banking Transaction API - Main Entry Point
 * Node.js / Express application
 */

const express = require('express');
const path = require('path');
const errorHandler = require('./middleware/errorHandler');
const transactionRoutes = require('./routes/transactions');
const accountRoutes = require('./routes/accounts');
const summaryRoutes = require('./routes/summary');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// CORS middleware
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  if (req.method === 'OPTIONS') {
    return res.sendStatus(200);
  }
  next();
});

// Serve static files from public folder
app.use(express.static(path.join(__dirname, '../public')));

// Request logging middleware
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  next();
});

// Routes
app.use('/transactions', transactionRoutes);
app.use('/accounts', accountRoutes);
app.use('/summary', summaryRoutes);

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    error: {
      status: 404,
      message: 'Endpoint not found'
    }
  });
});

// Error handling middleware
app.use(errorHandler);

// Start server
app.listen(PORT, () => {
  console.log(`🚀 Banking Transaction API running on http://localhost:${PORT}`);
  console.log(`📝 API Documentation:`);
  console.log(`   POST   /transactions           - Create new transaction`);
  console.log(`   GET    /transactions           - List all transactions (with filters)`);
  console.log(`   GET    /transactions/:id       - Get specific transaction`);
  console.log(`   GET    /accounts/:accountId/balance - Get account balance`);
  console.log(`   GET    /summary                - Get transaction summary`);
  console.log(`   GET    /health                 - Health check`);
});

module.exports = app;
