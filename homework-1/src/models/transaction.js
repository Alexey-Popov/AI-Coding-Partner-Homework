const { v4: uuidv4 } = require('uuid');

class Transaction {
  constructor(data) {
    this.id = uuidv4();
    this.fromAccount = data.fromAccount;
    this.toAccount = data.toAccount;
    this.amount = data.amount;
    this.currency = data.currency;
    this.type = data.type;
    this.timestamp = new Date().toISOString();
    this.status = data.status || 'completed';
  }
}

module.exports = Transaction;
