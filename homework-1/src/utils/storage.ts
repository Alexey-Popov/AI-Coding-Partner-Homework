import { v4 as uuidv4 } from 'uuid';
import { Transaction, CreateTransactionInput } from '../models';

// In-memory storage with proper types
const transactions: Transaction[] = [];
const accounts: Map<string, number> = new Map();

/**
 * Create a new transaction
 */
export function createTransaction(transactionData: CreateTransactionInput): Transaction {
  const transaction: Transaction = {
    id: uuidv4(),
    ...transactionData,
    timestamp: new Date().toISOString(),
    status: 'completed'
  };
  
  transactions.push(transaction);
  
  // Update account balances
  updateAccountBalances(transaction);
  
  return transaction;
}

/**
 * Get all transactions
 */
export function getAllTransactions(): Transaction[] {
  return [...transactions]; // Return a copy to prevent external modifications
}

/**
 * Get transaction by ID
 */
export function getTransactionById(id: string): Transaction | undefined {
  return transactions.find(t => t.id === id);
}

/**
 * Update account balances based on transaction
 */
function updateAccountBalances(transaction: Transaction): void {
  const { fromAccount, toAccount, amount, type } = transaction;
  
  // Initialize accounts if they don't exist
  if (fromAccount && !accounts.has(fromAccount)) {
    accounts.set(fromAccount, 0);
  }
  if (toAccount && !accounts.has(toAccount)) {
    accounts.set(toAccount, 0);
  }
  
  // Update balances based on transaction type
  switch (type) {
    case 'deposit':
      if (toAccount) {
        accounts.set(toAccount, accounts.get(toAccount)! + amount);
      }
      break;
    case 'withdrawal':
      if (fromAccount) {
        accounts.set(fromAccount, accounts.get(fromAccount)! - amount);
      }
      break;
    case 'transfer':
      if (fromAccount && toAccount) {
        accounts.set(fromAccount, accounts.get(fromAccount)! - amount);
        accounts.set(toAccount, accounts.get(toAccount)! + amount);
      }
      break;
  }
}

/**
 * Get account balance
 */
export function getAccountBalance(accountId: string): number | null {
  return accounts.has(accountId) ? accounts.get(accountId)! : null;
}

/**
 * Clear all storage (for testing purposes)
 */
export function clearStorage(): void {
  transactions.length = 0;
  accounts.clear();
}
