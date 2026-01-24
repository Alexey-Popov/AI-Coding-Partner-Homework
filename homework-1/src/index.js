const express = require('express');
const transactionsRouter = require('./routes/transactions');
const { getAccountBalance, getAccountSummary, getAccountInterest } = require('./routes/transactions');

const app = express();
app.use(express.json());

// Rate limiting: 100 requests per minute per IP
const rateLimitWindow = 60 * 1000;
const maxRequests = 100;
const requestCounts = new Map();

setInterval(() => {
  requestCounts.clear();
}, rateLimitWindow);

app.use((req, res, next) => {
  const ip = req.ip || req.socket.remoteAddress;
  const count = requestCounts.get(ip) || 0;

  if (count >= maxRequests) {
    return res.status(429).json({ error: 'Too Many Requests' });
  }

  requestCounts.set(ip, count + 1);
  next();
});

app.use('/transactions', transactionsRouter);
app.get('/accounts/:accountId/balance', getAccountBalance);
app.get('/accounts/:accountId/summary', getAccountSummary);
app.get('/accounts/:accountId/interest', getAccountInterest);

app.use((req, res) => {
  res.status(404).json({ error: 'Not found' });
});

app.use((err, req, res, next) => {
  console.error(err);
  res.status(500).json({ error: 'Internal Server Error' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
