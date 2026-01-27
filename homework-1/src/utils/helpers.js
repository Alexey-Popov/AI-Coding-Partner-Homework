/**
 * Calculates the balance for a specific account
 */
function calculateBalance(transactions, accountId) {
  let balance = 0;

  transactions.forEach(transaction => {
    if (transaction.status !== 'completed') return;

    // Add deposits
    if (transaction.toAccount === accountId && transaction.type === 'deposit') {
      balance += transaction.amount;
    }
    // Add transfers to this account
    else if (transaction.toAccount === accountId && transaction.type === 'transfer') {
      balance += transaction.amount;
    }
    // Subtract withdrawals
    else if (transaction.fromAccount === accountId && transaction.type === 'withdrawal') {
      balance -= transaction.amount;
    }
    // Subtract transfers from this account
    else if (transaction.fromAccount === accountId && transaction.type === 'transfer') {
      balance -= transaction.amount;
    }
  });

  return balance;
}

/**
 * Filters transactions based on query parameters
 */
function filterTransactions(transactions, filters) {
  let filtered = [...transactions];

  // Filter by account
  if (filters.accountId) {
    filtered = filtered.filter(t => 
      t.fromAccount === filters.accountId || t.toAccount === filters.accountId
    );
  }

  // Filter by type
  if (filters.type) {
    filtered = filtered.filter(t => t.type === filters.type.toLowerCase());
  }

  // Filter by date range
  if (filters.from || filters.to) {
    filtered = filtered.filter(t => {
      const transactionDate = new Date(t.timestamp);
      
      if (filters.from && filters.to) {
        const fromDate = new Date(filters.from);
        const toDate = new Date(filters.to);
        toDate.setHours(23, 59, 59, 999); // Include the entire end date
        return transactionDate >= fromDate && transactionDate <= toDate;
      } else if (filters.from) {
        const fromDate = new Date(filters.from);
        return transactionDate >= fromDate;
      } else if (filters.to) {
        const toDate = new Date(filters.to);
        toDate.setHours(23, 59, 59, 999);
        return transactionDate <= toDate;
      }
      
      return true;
    });
  }

  return filtered;
}

/**
 * Generates account summary statistics
 */
function getAccountSummary(transactions, accountId) {
  let totalDeposits = 0;
  let totalWithdrawals = 0;
  let transactionCount = 0;
  let mostRecentDate = null;

  transactions.forEach(transaction => {
    // Check if transaction involves this account
    const isInvolved = transaction.fromAccount === accountId || transaction.toAccount === accountId;
    if (!isInvolved || transaction.status !== 'completed') return;

    transactionCount++;

    // Update most recent date
    const transactionDate = new Date(transaction.timestamp);
    if (!mostRecentDate || transactionDate > mostRecentDate) {
      mostRecentDate = transactionDate;
    }

    // Calculate deposits and withdrawals
    if (transaction.toAccount === accountId) {
      totalDeposits += transaction.amount;
    }
    if (transaction.fromAccount === accountId) {
      totalWithdrawals += transaction.amount;
    }
  });

  return {
    totalDeposits: Math.round(totalDeposits * 100) / 100,
    totalWithdrawals: Math.round(totalWithdrawals * 100) / 100,
    transactionCount,
    mostRecentTransactionDate: mostRecentDate ? mostRecentDate.toISOString() : null
  };
}

module.exports = {
  calculateBalance,
  filterTransactions,
  getAccountSummary
};
