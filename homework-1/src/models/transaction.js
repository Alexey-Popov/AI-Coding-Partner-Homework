const { randomUUID } = require('crypto');

function createTransactionObject(data) {
  const now = new Date().toISOString();
  return {
    id: randomUUID(),
    fromAccount: typeof data.fromAccount === 'undefined' || data.fromAccount === null ? null : String(data.fromAccount),
    toAccount: typeof data.toAccount === 'undefined' || data.toAccount === null ? null : String(data.toAccount),
    amount: Number(data.amount),
    currency: String((data.currency || '').toUpperCase()),
    type: String(data.type),
    timestamp: data.timestamp || now,
    status: data.status || 'pending'
  };
}

module.exports = { createTransactionObject };
