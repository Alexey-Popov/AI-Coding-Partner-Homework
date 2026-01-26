export interface User {
  id: string;
  email: string;
  full_name: string | null;
  role: string;
  status: string;
  created_at: string;
}

export interface Account {
  id: string;
  user_id: string;
  card_number: string;
  currency: string;
  balance: string;
  created_at: string;
  updated_at: string;
}

export interface Transaction {
  id: string;
  created_at: string;
  type: string;
  source_account_id: string | null;
  target_account_id: string | null;
  source_amount: string | null;
  source_currency: string | null;
  target_amount: string | null;
  target_currency: string | null;
  fx_rate: string | null;
  description: string | null;
  status: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  full_name?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  access_token: string;
  token_type: string;
}

export interface CreateAccountRequest {
  currency: string;
  initial_balance: string;
  card_number?: string;
}

export interface TransferRequest {
  source_card_number: string;
  target_card_number: string;
  source_currency: string;
  source_amount: string;
  target_currency: string;
  fx_rate: string;
  target_amount?: string;
  description?: string;
}

export interface TransferResponse {
  transaction_id: string;
  status: string;
  source_account: Account;
  target_account: Account;
}

export interface TransactionListResponse {
  transactions: Transaction[];
  total: number;
  limit: number;
  offset: number;
}

export interface ErrorResponse {
  detail: string;
}

// Admin types
export interface AdminUserSummary {
  id: string;
  email: string;
  full_name: string | null;
  role: string;
  status: string;
  created_at: string;
  updated_at: string;
}

export interface AdminUserListResponse {
  users: AdminUserSummary[];
  total: number;
  limit: number;
  offset: number;
}

export interface AdminTransactionSummary {
  id: string;
  created_at: string;
  type: string;
  source_account_id: string | null;
  target_account_id: string | null;
  source_amount: number | null;
  source_currency: string | null;
  target_amount: number | null;
  target_currency: string | null;
  fx_rate: number | null;
  description: string | null;
  status: string;
  source_user_id: string | null;
  target_user_id: string | null;
  source_card_number: string | null;
  target_card_number: string | null;
}

export interface AdminTransactionListResponse {
  transactions: AdminTransactionSummary[];
  total: number;
  limit: number;
  offset: number;
}
