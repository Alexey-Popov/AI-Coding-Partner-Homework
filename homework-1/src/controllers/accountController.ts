import { TransactionType } from '../models/transaction';
import { BalanceResponse, AccountSummary } from '../models/balance';
import { getTransactionsByAccount } from '../utils/dataStore';

/**
 * Calculate account balance from transactions
 */
export function calculateAccountBalance(accountId: string): BalanceResponse {
  const transactions = getTransactionsByAccount(accountId);

  // Calculate balance
  let balance = 0;
  let currency = 'USD'; // Default currency

  for (const transaction of transactions) {
    currency = transaction.currency; // Use the currency from transactions

    switch (transaction.type) {
      case TransactionType.DEPOSIT:
        // Deposits add to the account
        if (transaction.toAccount === accountId) {
          balance += transaction.amount;
        }
        break;

      case TransactionType.WITHDRAWAL:
        // Withdrawals subtract from the account
        if (transaction.fromAccount === accountId) {
          balance -= transaction.amount;
        }
        break;

      case TransactionType.TRANSFER:
        // Transfers out subtract, transfers in add
        if (transaction.fromAccount === accountId) {
          balance -= transaction.amount;
        }
        if (transaction.toAccount === accountId) {
          balance += transaction.amount;
        }
        break;
    }
  }

  return {
    accountId,
    balance: Math.round(balance * 100) / 100, // Round to 2 decimal places
    currency,
    transactionCount: transactions.length
  };
}

/**
 * Get account transaction summary with aggregated statistics
 */
export function getAccountSummary(accountId: string): AccountSummary {
  const transactions = getTransactionsByAccount(accountId);

  // Initialize aggregates
  const totalDeposits: Record<string, number> = {};
  const totalWithdrawals: Record<string, number> = {};
  let mostRecentTransactionDate: string | null = null;

  for (const transaction of transactions) {
    const currency = transaction.currency;

    // Track most recent transaction
    if (!mostRecentTransactionDate || transaction.timestamp > mostRecentTransactionDate) {
      mostRecentTransactionDate = transaction.timestamp;
    }

    switch (transaction.type) {
      case TransactionType.DEPOSIT:
        // Deposits add to the account (all statuses)
        if (transaction.toAccount === accountId) {
          totalDeposits[currency] = (totalDeposits[currency] || 0) + transaction.amount;
        }
        break;

      case TransactionType.WITHDRAWAL:
        // Withdrawals subtract from the account (all statuses)
        if (transaction.fromAccount === accountId) {
          totalWithdrawals[currency] = (totalWithdrawals[currency] || 0) + transaction.amount;
        }
        break;

      case TransactionType.TRANSFER:
        // Transfers: track separately for deposits and withdrawals
        if (transaction.fromAccount === accountId) {
          totalWithdrawals[currency] = (totalWithdrawals[currency] || 0) + transaction.amount;
        }
        if (transaction.toAccount === accountId) {
          totalDeposits[currency] = (totalDeposits[currency] || 0) + transaction.amount;
        }
        break;
    }
  }

  // Round all amounts to 2 decimal places
  for (const currency in totalDeposits) {
    totalDeposits[currency] = Math.round(totalDeposits[currency] * 100) / 100;
  }
  for (const currency in totalWithdrawals) {
    totalWithdrawals[currency] = Math.round(totalWithdrawals[currency] * 100) / 100;
  }

  return {
    accountId,
    totalDeposits,
    totalWithdrawals,
    transactionCount: transactions.length,
    mostRecentTransactionDate
  };
}
