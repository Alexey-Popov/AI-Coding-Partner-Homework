const express = require('express');
const router = express.Router();
const { transactions } = require('../utils/helpers');
const { validateTransaction } = require('../validators/transactionValidator');
const { createTransactionObject } = require('../models/transaction');

router.post('/', (req, res) => {
  const data = req.body;
  const errors = validateTransaction(data);
  if (errors.length) return res.status(400).json({ error: 'Validation failed', details: errors });

  const tx = createTransactionObject(data);
  transactions.push(tx);
  return res.status(201).json(tx);
});

router.get('/', (req, res) => {
  const { accountId, type, from, to } = req.query;
  const errors = [];
  let fromDate, toDate;

  if (from) {
    const d = Date.parse(from);
    if (Number.isNaN(d)) errors.push({ field: 'from', message: 'Invalid from date (must be ISO 8601)' });
    else fromDate = new Date(d);
  }
  if (to) {
    const d = Date.parse(to);
    if (Number.isNaN(d)) errors.push({ field: 'to', message: 'Invalid to date (must be ISO 8601)' });
    else toDate = new Date(d);
  }

  if (errors.length) return res.status(400).json({ error: 'Invalid query', details: errors });

  const filtered = transactions.filter(t => {
    // filter by accountId (matches either fromAccount or toAccount)
    if (accountId) {
      if (!(t.fromAccount === accountId || t.toAccount === accountId)) return false;
    }

    // filter by type
    if (type) {
      if (t.type !== type) return false;
    }

    // filter by date range
    if (fromDate || toDate) {
      const ts = t.timestamp ? new Date(t.timestamp) : null;
      if (!ts) return false;
      if (fromDate && ts < fromDate) return false;
      if (toDate && ts > toDate) return false;
    }

    return true;
  });

  res.json(filtered);
});

router.get('/export', (req, res) => {
  const { format } = req.query;

  if (!format) {
    return res.status(400).json({ error: 'format query parameter is required' });
  }

  if (format !== 'csv') {
    return res.status(400).json({ error: `Unsupported format: ${format}. Supported formats: csv` });
  }

  if (transactions.length === 0) {
    return res.status(404).json({ error: 'No transactions to export' });
  }

  const headers = ['id', 'fromAccount', 'toAccount', 'amount', 'currency', 'type', 'timestamp', 'status'];
  const csvRows = [headers.join(',')];

  for (const t of transactions) {
    const row = headers.map(h => {
      const val = t[h];
      if (val === null || val === undefined) return '';
      const str = String(val);
      if (str.includes(',') || str.includes('"') || str.includes('\n')) {
        return `"${str.replace(/"/g, '""')}"`;
      }
      return str;
    });
    csvRows.push(row.join(','));
  }

  const csv = csvRows.join('\n');

  res.setHeader('Content-Type', 'text/csv');
  res.setHeader('Content-Disposition', 'attachment; filename="transactions.csv"');
  res.send(csv);
});

router.get('/:id', (req, res) => {
  const tx = transactions.find(t => t.id === req.params.id);
  if (!tx) return res.status(404).json({ error: 'Transaction not found' });
  res.json(tx);
});

function getAccountBalance(req, res) {
  const { accountId } = req.params;
  if (!accountId) return res.status(400).json({ error: 'accountId required' });

  let balance = 0;
  for (const t of transactions) {
    const amt = Number(t.amount) || 0;
    if (t.type === 'deposit' && t.toAccount === accountId) balance += amt;
    else if (t.type === 'withdrawal' && t.fromAccount === accountId) balance -= amt;
    else if (t.type === 'transfer') {
      if (t.fromAccount === accountId) balance -= amt;
      if (t.toAccount === accountId) balance += amt;
    }
  }

  res.json({ accountId, balance });
}

function getAccountSummary(req, res) {
  const { accountId } = req.params;
  if (!accountId) return res.status(400).json({ error: 'accountId required' });

  let totalDeposits = 0;
  let totalWithdrawals = 0;
  let transactionCount = 0;
  let mostRecentDate = null;

  for (const t of transactions) {
    const isFromAccount = t.fromAccount === accountId;
    const isToAccount = t.toAccount === accountId;

    if (!isFromAccount && !isToAccount) continue;

    transactionCount++;
    const amt = Number(t.amount) || 0;

    if (t.type === 'deposit' && isToAccount) {
      totalDeposits += amt;
    } else if (t.type === 'withdrawal' && isFromAccount) {
      totalWithdrawals += amt;
    } else if (t.type === 'transfer') {
      if (isFromAccount) totalWithdrawals += amt;
      if (isToAccount) totalDeposits += amt;
    }

    if (t.timestamp) {
      const txDate = new Date(t.timestamp);
      if (!mostRecentDate || txDate > mostRecentDate) {
        mostRecentDate = txDate;
      }
    }
  }

  if (transactionCount === 0) {
    return res.status(404).json({ error: `Account ${accountId} not found` });
  }

  res.json({
    accountId,
    totalDeposits,
    totalWithdrawals,
    transactionCount,
    mostRecentTransactionDate: mostRecentDate ? mostRecentDate.toISOString() : null
  });
}

function getAccountInterest(req, res) {
  const { accountId } = req.params;
  const { rate, days } = req.query;

  if (!accountId) return res.status(400).json({ error: 'accountId required' });

  const errors = [];
  if (rate === undefined) errors.push({ field: 'rate', message: 'rate is required' });
  if (days === undefined) errors.push({ field: 'days', message: 'days is required' });

  const rateNum = Number(rate);
  const daysNum = Number(days);

  if (rate !== undefined && (isNaN(rateNum) || rateNum < 0)) {
    errors.push({ field: 'rate', message: 'rate must be a non-negative number' });
  }
  if (days !== undefined && (isNaN(daysNum) || daysNum < 0 || !Number.isInteger(daysNum))) {
    errors.push({ field: 'days', message: 'days must be a non-negative integer' });
  }

  if (errors.length) return res.status(400).json({ error: 'Validation failed', details: errors });

  let balance = 0;
  let hasTransactions = false;

  for (const t of transactions) {
    const amt = Number(t.amount) || 0;
    if (t.type === 'deposit' && t.toAccount === accountId) {
      balance += amt;
      hasTransactions = true;
    } else if (t.type === 'withdrawal' && t.fromAccount === accountId) {
      balance -= amt;
      hasTransactions = true;
    } else if (t.type === 'transfer') {
      if (t.fromAccount === accountId) {
        balance -= amt;
        hasTransactions = true;
      }
      if (t.toAccount === accountId) {
        balance += amt;
        hasTransactions = true;
      }
    }
  }

  if (!hasTransactions) {
    return res.status(404).json({ error: `Account ${accountId} not found` });
  }

  const interest = balance * rateNum * (daysNum / 365);

  res.json({
    accountId,
    balance,
    rate: rateNum,
    days: daysNum,
    interest: Math.round(interest * 100) / 100
  });
}

module.exports = router;
module.exports.getAccountBalance = getAccountBalance;
module.exports.getAccountSummary = getAccountSummary;
module.exports.getAccountInterest = getAccountInterest;
