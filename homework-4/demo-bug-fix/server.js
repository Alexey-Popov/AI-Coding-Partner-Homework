/**
 * Demo API Server
 * Simple Express server for demonstrating multi-agent bug fixing
 */

const express = require('express');
const userRoutes = require('./src/routes/users');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(express.json());

// Routes
app.use(userRoutes);

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', message: 'Demo API is running' });
});

// Start server
app.listen(PORT, () => {
  console.log(`Demo API server running on http://localhost:${PORT}`);
  console.log('Try:');
  console.log(`  GET http://localhost:${PORT}/api/users`);
  console.log(`  GET http://localhost:${PORT}/api/users/123`);
});

module.exports = app;
