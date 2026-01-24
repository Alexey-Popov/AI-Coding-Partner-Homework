import { randomUUID } from 'crypto';
import { Transaction, CreateTransactionDTO, TransactionStatus } from '../models/transaction';

// In-memory storage for transactions
const transactions: Transaction[] = [];

/**
 * Create a new transaction and add it to the store
 */
export function createTransaction(dto: CreateTransactionDTO): Transaction {
  const transaction: Transaction = {
    id: randomUUID(),
    fromAccount: dto.fromAccount,
    toAccount: dto.toAccount,
    amount: dto.amount,
    currency: dto.currency,
    type: dto.type,
    timestamp: new Date().toISOString(),
    status: TransactionStatus.COMPLETED
  };

  transactions.push(transaction);
  return transaction;
}

/**
 * Get all transactions
 */
export function getAllTransactions(): Transaction[] {
  return [...transactions];
}

/**
 * Get a transaction by ID
 */
export function getTransactionById(id: string): Transaction | undefined {
  return transactions.find(t => t.id === id);
}

/**
 * Get all transactions for a specific account
 */
export function getTransactionsByAccount(accountId: string): Transaction[] {
  return transactions.filter(
    t => t.fromAccount === accountId || t.toAccount === accountId
  );
}
