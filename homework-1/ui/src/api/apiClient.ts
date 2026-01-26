import {
  RegisterRequest,
  LoginRequest,
  TokenResponse,
  User,
  Account,
  CreateAccountRequest,
  TransferRequest,
  TransferResponse,
  TransactionListResponse,
  ErrorResponse,
  AdminUserListResponse,
  AdminUserSummary,
  AdminTransactionListResponse
} from './types';

// API Configuration
// In production (Mode A), the frontend is served by the backend, so we use relative URLs
// In development, VITE_API_BASE_URL can point to the dev server
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 
                     (import.meta.env.DEV ? 'http://localhost:8000' : '');

class ApiClient {
  private getHeaders(includeAuth: boolean = false): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    if (includeAuth) {
      const token = localStorage.getItem('access_token');
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    }

    return headers;
  }

  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      const error: ErrorResponse = await response.json();
      throw new Error(error.detail || 'An error occurred');
    }
    return response.json();
  }

  async register(data: RegisterRequest): Promise<User> {
    const response = await fetch(`${API_BASE_URL}/v1/auth/register`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(data),
    });
    return this.handleResponse<User>(response);
  }

  async login(data: LoginRequest): Promise<TokenResponse> {
    const response = await fetch(`${API_BASE_URL}/v1/auth/login`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(data),
    });
    return this.handleResponse<TokenResponse>(response);
  }

  async getCurrentUser(): Promise<User> {
    const response = await fetch(`${API_BASE_URL}/v1/auth/me`, {
      headers: this.getHeaders(true),
    });
    return this.handleResponse<User>(response);
  }

  async getAccounts(): Promise<{ accounts: Account[] }> {
    const response = await fetch(`${API_BASE_URL}/v1/accounts`, {
      headers: this.getHeaders(true),
    });
    return this.handleResponse<{ accounts: Account[] }>(response);
  }

  async getAccount(accountId: string): Promise<Account> {
    const response = await fetch(`${API_BASE_URL}/v1/accounts/${accountId}`, {
      headers: this.getHeaders(true),
    });
    return this.handleResponse<Account>(response);
  }

  async createAccount(data: CreateAccountRequest): Promise<Account> {
    const response = await fetch(`${API_BASE_URL}/v1/accounts`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify(data),
    });
    return this.handleResponse<Account>(response);
  }

  async getAccountTransactions(
    accountId: string,
    limit: number = 20,
    offset: number = 0
  ): Promise<TransactionListResponse> {
    const response = await fetch(
      `${API_BASE_URL}/v1/accounts/${accountId}/transactions?limit=${limit}&offset=${offset}`,
      {
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse<TransactionListResponse>(response);
  }

  async transfer(data: TransferRequest): Promise<TransferResponse> {
    const response = await fetch(`${API_BASE_URL}/v1/transfers`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify(data),
    });
    return this.handleResponse<TransferResponse>(response);
  }

  // Admin endpoints
  async adminListUsers(
    limit: number = 20,
    offset: number = 0,
    status?: string,
    emailContains?: string
  ): Promise<AdminUserListResponse> {
    const params = new URLSearchParams({
      limit: limit.toString(),
      offset: offset.toString(),
    });
    if (status) params.append('status', status);
    if (emailContains) params.append('email_contains', emailContains);

    const response = await fetch(
      `${API_BASE_URL}/v1/admin/users?${params.toString()}`,
      {
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse<AdminUserListResponse>(response);
  }

  async adminBlockUser(userId: string): Promise<AdminUserSummary> {
    const response = await fetch(
      `${API_BASE_URL}/v1/admin/users/${userId}/block`,
      {
        method: 'PATCH',
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse<AdminUserSummary>(response);
  }

  async adminUnblockUser(userId: string): Promise<AdminUserSummary> {
    const response = await fetch(
      `${API_BASE_URL}/v1/admin/users/${userId}/unblock`,
      {
        method: 'PATCH',
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse<AdminUserSummary>(response);
  }

  async adminListTransactions(
    limit: number = 20,
    offset: number = 0,
    filters?: {
      user_id?: string;
      account_id?: string;
      type?: string;
      status?: string;
      from_date?: string;
      to_date?: string;
    }
  ): Promise<AdminTransactionListResponse> {
    const params = new URLSearchParams({
      limit: limit.toString(),
      offset: offset.toString(),
    });
    if (filters?.user_id) params.append('user_id', filters.user_id);
    if (filters?.account_id) params.append('account_id', filters.account_id);
    if (filters?.type) params.append('type', filters.type);
    if (filters?.status) params.append('status', filters.status);
    if (filters?.from_date) params.append('from_date', filters.from_date);
    if (filters?.to_date) params.append('to_date', filters.to_date);

    const response = await fetch(
      `${API_BASE_URL}/v1/admin/transactions?${params.toString()}`,
      {
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse<AdminTransactionListResponse>(response);
  }
}

export const apiClient = new ApiClient();
