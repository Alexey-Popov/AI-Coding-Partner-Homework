// Supported currency codes (ISO 4217)
export type Currency = 'USD' | 'EUR' | 'GBP' | 'JPY';

// Transaction type enum
export enum TransactionType {
  DEPOSIT = 'deposit',
  WITHDRAWAL = 'withdrawal',
  TRANSFER = 'transfer'
}

// Transaction status enum
export enum TransactionStatus {
  PENDING = 'pending',
  COMPLETED = 'completed',
  FAILED = 'failed'
}

// Complete transaction interface
export interface Transaction {
  id: string;
  fromAccount?: string;
  toAccount?: string;
  amount: number;
  currency: Currency;
  type: TransactionType;
  timestamp: string; // ISO 8601 format
  status: TransactionStatus;
}

// DTO for creating a new transaction
export interface CreateTransactionDTO {
  fromAccount?: string;
  toAccount?: string;
  amount: number;
  currency: Currency;
  type: TransactionType;
}

// Query parameters for filtering transactions
export interface TransactionQueryParams {
  accountId?: string;
  type?: string;
  from?: string;
  to?: string;
}
