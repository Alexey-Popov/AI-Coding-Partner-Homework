import {
  Transaction,
  CreateTransactionInput,
  TransactionStatus,
  TransactionType,
  TransactionFilters
} from '../models/transaction';
import { storageService } from './storageService';
import { validateTransaction } from '../validators/transactionValidator';

/**
 * Transaction service handling business logic
 */
class TransactionService {
  /**
   * Create a new transaction
   */
  createTransaction(input: CreateTransactionInput): { transaction?: Transaction; errors?: any } {
    // Validate input
    const validationErrors = validateTransaction(input);
    if (validationErrors.length > 0) {
      return {
        errors: {
          error: 'Validation failed',
          details: validationErrors
        }
      };
    }

    // Create transaction
    const transaction: Transaction = {
      id: storageService.generateTransactionId(),
      fromAccount: input.fromAccount,
      toAccount: input.toAccount,
      amount: input.amount,
      currency: input.currency,
      type: input.type,
      timestamp: new Date().toISOString(),
      status: TransactionStatus.COMPLETED
    };

    // Update account balances based on transaction type
    this.updateAccountBalances(transaction);

    // Save transaction
    storageService.saveTransaction(transaction);

    return { transaction };
  }

  /**
   * Get all transactions with optional filtering
   */
  getTransactions(filters?: TransactionFilters): Transaction[] {
    let transactions = storageService.getAllTransactions();

    if (!filters) {
      return transactions;
    }

    // Filter by accountId (transactions involving this account)
    if (filters.accountId) {
      transactions = transactions.filter(
        txn => txn.fromAccount === filters.accountId || txn.toAccount === filters.accountId
      );
    }

    // Filter by type
    if (filters.type) {
      transactions = transactions.filter(txn => txn.type === filters.type);
    }

    // Filter by date range
    if (filters.from) {
      const fromDate = new Date(filters.from);
      transactions = transactions.filter(txn => new Date(txn.timestamp) >= fromDate);
    }

    if (filters.to) {
      const toDate = new Date(filters.to);
      transactions = transactions.filter(txn => new Date(txn.timestamp) <= toDate);
    }

    return transactions;
  }

  /**
   * Get a transaction by ID
   */
  getTransactionById(id: string): Transaction | undefined {
    return storageService.getTransactionById(id);
  }

  /**
   * Update account balances based on transaction
   */
  private updateAccountBalances(transaction: Transaction): void {
    const { fromAccount, toAccount, amount, currency, type } = transaction;

    switch (type) {
      case TransactionType.DEPOSIT:
        // Deposit: increase toAccount balance
        storageService.updateAccountBalance(toAccount, amount, currency);
        break;

      case TransactionType.WITHDRAWAL:
        // Withdrawal: decrease fromAccount balance
        storageService.updateAccountBalance(fromAccount, -amount, currency);
        break;

      case TransactionType.TRANSFER:
        // Transfer: decrease fromAccount, increase toAccount
        storageService.updateAccountBalance(fromAccount, -amount, currency);
        storageService.updateAccountBalance(toAccount, amount, currency);
        break;
    }
  }

  /**
   * Get transaction statistics for an account
   */
  getTransactionStats(accountId: string): {
    totalDeposits: number;
    totalWithdrawals: number;
    transactionCount: number;
    mostRecentTransaction?: string;
  } {
    const transactions = this.getTransactions({ accountId });

    let totalDeposits = 0;
    let totalWithdrawals = 0;
    let mostRecentTransaction: string | undefined;

    transactions.forEach(txn => {
      if (txn.toAccount === accountId && txn.type !== TransactionType.WITHDRAWAL) {
        totalDeposits += txn.amount;
      }
      if (txn.fromAccount === accountId && txn.type !== TransactionType.DEPOSIT) {
        totalWithdrawals += txn.amount;
      }

      if (!mostRecentTransaction || txn.timestamp > mostRecentTransaction) {
        mostRecentTransaction = txn.timestamp;
      }
    });

    return {
      totalDeposits,
      totalWithdrawals,
      transactionCount: transactions.length,
      mostRecentTransaction
    };
  }
}

export const transactionService = new TransactionService();

