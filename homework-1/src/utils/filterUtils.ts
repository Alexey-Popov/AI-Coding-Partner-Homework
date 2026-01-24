import { Transaction } from '../models/transaction';

export function filterByType(transactions: Transaction[], type: string | undefined): Transaction[] {
  if (!type || type.trim() === '') {
    return transactions;
  }

  const normalizedType = type.toLowerCase();
  return transactions.filter(t => t.type.toLowerCase() === normalizedType);
}

export function filterByDateRange(
  transactions: Transaction[],
  fromDate: Date | null,
  toDate: Date | null
): Transaction[] {
  if (!fromDate && !toDate) {
    return transactions;
  }

  return transactions.filter(t => {
    const transactionDate = new Date(t.timestamp);
    
    if (fromDate && transactionDate < fromDate) {
      return false;
    }
    
    if (toDate && transactionDate > toDate) {
      return false;
    }
    
    return true;
  });
}
