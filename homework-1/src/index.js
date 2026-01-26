const express = require('express');
const transactionRoutes = require('./routes/transactions');
const accountRoutes = require('./routes/accounts');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(express.json());

// Routes
app.use('/transactions', transactionRoutes);
app.use('/accounts', accountRoutes);

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Not found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: 'Internal server error' });
});

app.listen(PORT, () => {
  console.log(`Banking Transactions API running on port ${PORT}`);
});

module.exports = app;
