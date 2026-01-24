// Balance response structure
export interface BalanceResponse {
  accountId: string;
  balance: number;
  currency: string;
  transactionCount: number;
}

// Account summary response structure
export interface AccountSummary {
  accountId: string;
  totalDeposits: Record<string, number>; // Total deposits by currency
  totalWithdrawals: Record<string, number>; // Total withdrawals by currency
  transactionCount: number;
  mostRecentTransactionDate: string | null;
}
