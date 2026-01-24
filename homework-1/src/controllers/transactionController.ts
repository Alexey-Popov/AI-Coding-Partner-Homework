import { Transaction, CreateTransactionDTO, TransactionQueryParams } from '../models/transaction';
import { ErrorResponse } from '../models/common';
import {
  createTransaction as createTransactionInStore,
  getAllTransactions,
  getTransactionById as getTransactionByIdFromStore,
  getTransactionsByAccount
} from '../utils/dataStore';
import { filterByType, filterByDateRange } from '../utils/filterUtils';
import { parseFromDate, parseToDate } from '../utils/dateUtils';
import { convertToCSV } from '../utils/csvUtils';
import { validateTransaction } from '../validators/transactionValidator';

/**
 * Get filtered transactions based on query parameters
 * Applies filters in sequence: account -> type -> date range
 */
export function getFilteredTransactions(params: TransactionQueryParams): Transaction[] {
  const { accountId, type, from, to } = params;

  let filteredTransactions: Transaction[];

  if (accountId && accountId.trim() !== '') {
    filteredTransactions = getTransactionsByAccount(accountId);
  } else {
    filteredTransactions = getAllTransactions();
  }

  filteredTransactions = filterByType(filteredTransactions, type);

  const fromDate = parseFromDate(from);
  const toDate = parseToDate(to);
  filteredTransactions = filterByDateRange(filteredTransactions, fromDate, toDate);

  return filteredTransactions;
}

/**
 * Create a new transaction with validation
 * @returns Transaction object if successful, ErrorResponse if validation fails
 */
export function createTransaction(dto: CreateTransactionDTO): Transaction | ErrorResponse {
  // Validate the transaction
  const validationError = validateTransaction(dto);
  if (validationError) {
    return validationError;
  }

  // Create and return the transaction
  return createTransactionInStore(dto);
}

/**
 * Get a transaction by ID
 * @returns Transaction if found, null otherwise
 */
export function getTransactionById(id: string): Transaction | null {
  return getTransactionByIdFromStore(id) || null;
}

/**
 * Export transactions as CSV with optional filtering
 * @returns Object containing CSV string and filename
 */
export function exportTransactionsAsCSV(params: TransactionQueryParams): { csv: string; filename: string } {
  const filteredTransactions = getFilteredTransactions(params);
  const csv = convertToCSV(filteredTransactions);
  
  // Generate filename with current date
  const dateStr = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
  const filename = `transactions-${dateStr}.csv`;

  return { csv, filename };
}
