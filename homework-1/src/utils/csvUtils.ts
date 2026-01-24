import { Transaction } from '../models/transaction';

export function convertToCSV(transactions: Transaction[]): string {
  const headers = 'id,fromAccount,toAccount,amount,currency,type,timestamp,status';
  
  if (transactions.length === 0) {
    return headers;
  }
  
  const rows = transactions.map(t => {
    return [
      t.id,
      t.fromAccount || '',
      t.toAccount || '',
      t.amount,
      t.currency,
      t.type,
      t.timestamp,
      t.status
    ].join(',');
  });
  
  return [headers, ...rows].join('\n');
}
