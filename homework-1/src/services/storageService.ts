import { Transaction, Account } from '../models/transaction';

/**
 * In-memory storage service for transactions and accounts
 */
class StorageService {
  private transactions: Transaction[] = [];
  private accounts: Map<string, Account> = new Map();
  private transactionIdCounter: number = 1;

  /**
   * Generate a unique transaction ID
   */
  generateTransactionId(): string {
    const id = `TXN-${String(this.transactionIdCounter).padStart(8, '0')}`;
    this.transactionIdCounter++;
    return id;
  }

  /**
   * Save a transaction to storage
   */
  saveTransaction(transaction: Transaction): Transaction {
    this.transactions.push(transaction);
    return transaction;
  }

  /**
   * Get all transactions
   */
  getAllTransactions(): Transaction[] {
    return [...this.transactions];
  }

  /**
   * Get a transaction by ID
   */
  getTransactionById(id: string): Transaction | undefined {
    return this.transactions.find(txn => txn.id === id);
  }

  /**
   * Get or create an account
   */
  getOrCreateAccount(accountId: string, currency: string): Account {
    if (!this.accounts.has(accountId)) {
      const account: Account = {
        accountId,
        balance: 0,
        currency
      };
      this.accounts.set(accountId, account);
    }
    return this.accounts.get(accountId)!;
  }

  /**
   * Get an account by ID
   */
  getAccount(accountId: string): Account | undefined {
    return this.accounts.get(accountId);
  }

  /**
   * Update account balance
   */
  updateAccountBalance(accountId: string, amount: number, currency: string): void {
    const account = this.getOrCreateAccount(accountId, currency);
    account.balance += amount;
  }

  /**
   * Get all accounts
   */
  getAllAccounts(): Account[] {
    return Array.from(this.accounts.values());
  }

  /**
   * Clear all data (useful for testing)
   */
  clear(): void {
    this.transactions = [];
    this.accounts.clear();
    this.transactionIdCounter = 1;
  }

  /**
   * Get transaction count
   */
  getTransactionCount(): number {
    return this.transactions.length;
  }

  /**
   * Check if account exists
   */
  accountExists(accountId: string): boolean {
    return this.accounts.has(accountId);
  }
}

// Export singleton instance
export const storageService = new StorageService();

