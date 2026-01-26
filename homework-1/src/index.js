const express = require('express');
const path = require('path');
const transactionRoutes = require('./backend/routes/transactions');
const accountRoutes = require('./backend/routes/accounts');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(express.json());

// Serve static frontend files
app.use(express.static(path.join(__dirname, 'frontend')));

// API Routes
app.use('/transactions', transactionRoutes);
app.use('/accounts', accountRoutes);

// Serve frontend for root
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'frontend', 'index.html'));
});

// API health check
app.get('/api/health', (req, res) => {
  res.json({ message: 'Banking Transactions API is running' });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Endpoint not found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: 'Internal server error' });
});

app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});

module.exports = app;
