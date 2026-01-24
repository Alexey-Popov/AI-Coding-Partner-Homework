class Transaction {
  constructor(fromAccount, toAccount, amount, currency, type) {
    this.id = null; // Will be set by storage
    this.fromAccount = fromAccount;
    this.toAccount = toAccount;
    this.amount = amount;
    this.currency = currency;
    this.type = type; // deposit | withdrawal | transfer
    this.timestamp = new Date().toISOString();
    this.status = 'pending'; // pending | completed | failed
  }
}

module.exports = Transaction;
