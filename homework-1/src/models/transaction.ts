/**
 * Transaction types supported by the banking system
 */
export enum TransactionType {
  DEPOSIT = 'deposit',
  WITHDRAWAL = 'withdrawal',
  TRANSFER = 'transfer'
}

/**
 * Transaction status lifecycle
 */
export enum TransactionStatus {
  PENDING = 'pending',
  COMPLETED = 'completed',
  FAILED = 'failed'
}

/**
 * Main transaction interface matching API specification
 */
export interface Transaction {
  id: string;
  fromAccount: string;
  toAccount: string;
  amount: number;
  currency: string;
  type: TransactionType;
  timestamp: string;
  status: TransactionStatus;
}

/**
 * Input data for creating a new transaction
 */
export interface CreateTransactionInput {
  fromAccount: string;
  toAccount: string;
  amount: number;
  currency: string;
  type: TransactionType;
}

/**
 * Account balance information
 */
export interface Account {
  accountId: string;
  balance: number;
  currency: string;
}

/**
 * Query parameters for filtering transactions
 */
export interface TransactionFilters {
  accountId?: string;
  type?: TransactionType;
  from?: string;
  to?: string;
}

/**
 * Validation error detail
 */
export interface ValidationError {
  field: string;
  message: string;
}

/**
 * Error response structure
 */
export interface ErrorResponse {
  error: string;
  details?: ValidationError[];
}

