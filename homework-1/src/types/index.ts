export type TransactionType = 'deposit' | 'withdrawal' | 'transfer';
export type TransactionStatus = 'pending' | 'completed' | 'failed';

export interface Transaction {
  id: string;
  fromAccount?: string;
  toAccount?: string;
  amount: number;
  currency: string;
  type: TransactionType;
  timestamp: string;
  status: TransactionStatus;
  description?: string;
}

export interface CreateTransactionInput {
  fromAccount?: string;
  toAccount?: string;
  amount: number;
  currency: string;
  type: TransactionType;
  description?: string;
}

export interface AccountBalance {
  accountId: string;
  balance: number;
  currency: string;
}

export interface ValidationError {
  field: string;
  message: string;
}

export interface ErrorResponse {
  error: string;
  details?: ValidationError[];
  message?: string;
}
