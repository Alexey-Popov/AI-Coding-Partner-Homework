import { Account } from '../models/transaction';
import { storageService } from './storageService';

/**
 * Account service handling account-related operations
 */
class AccountService {
  /**
   * Get account balance
   */
  getAccountBalance(accountId: string): { balance: number; currency: string } | null {
    const account = storageService.getAccount(accountId);
    
    if (!account) {
      return null;
    }

    return {
      balance: account.balance,
      currency: account.currency
    };
  }

  /**
   * Get or create an account
   */
  getOrCreateAccount(accountId: string, currency: string = 'USD'): Account {
    return storageService.getOrCreateAccount(accountId, currency);
  }

  /**
   * Check if account exists
   */
  accountExists(accountId: string): boolean {
    return storageService.accountExists(accountId);
  }

  /**
   * Get all accounts
   */
  getAllAccounts(): Account[] {
    return storageService.getAllAccounts();
  }
}

export const accountService = new AccountService();

