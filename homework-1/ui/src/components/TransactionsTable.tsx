import { Transaction } from '../api/types';

interface TransactionsTableProps {
  transactions: Transaction[];
  currentAccountId: string;
}

export function TransactionsTable({ transactions, currentAccountId }: TransactionsTableProps) {
  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleString();
  };

  const getTransactionDirection = (transaction: Transaction): 'in' | 'out' => {
    if (transaction.target_account_id === currentAccountId) {
      return 'in';
    }
    return 'out';
  };

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: 'white' }}>
        <thead>
          <tr style={{ backgroundColor: '#f8f9fa', borderBottom: '2px solid #dee2e6' }}>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Date</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Type</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Amount</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Currency</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Status</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Description</th>
          </tr>
        </thead>
        <tbody>
          {transactions.length === 0 ? (
            <tr>
              <td colSpan={6} style={{ padding: '2rem', textAlign: 'center', color: '#6c757d' }}>
                No transactions found
              </td>
            </tr>
          ) : (
            transactions.map((transaction) => {
              const direction = getTransactionDirection(transaction);
              const amount = direction === 'in' ? transaction.target_amount : transaction.source_amount;
              const currency = direction === 'in' ? transaction.target_currency : transaction.source_currency;

              return (
                <tr key={transaction.id} style={{ borderBottom: '1px solid #dee2e6' }}>
                  <td style={{ padding: '1rem' }}>{formatDate(transaction.created_at)}</td>
                  <td style={{ padding: '1rem' }}>
                    <span style={{
                      backgroundColor: direction === 'in' ? '#d4edda' : '#f8d7da',
                      color: direction === 'in' ? '#155724' : '#721c24',
                      padding: '0.25rem 0.5rem',
                      borderRadius: '4px',
                      fontSize: '0.85rem'
                    }}>
                      {direction === 'in' ? 'INCOMING' : 'OUTGOING'}
                    </span>
                  </td>
                  <td style={{
                    padding: '1rem',
                    fontWeight: 'bold',
                    color: direction === 'in' ? '#28a745' : '#dc3545'
                  }}>
                    {direction === 'in' ? '+' : '-'}{amount}
                  </td>
                  <td style={{ padding: '1rem' }}>{currency}</td>
                  <td style={{ padding: '1rem' }}>
                    <span style={{
                      backgroundColor: transaction.status === 'COMPLETED' ? '#d4edda' : '#fff3cd',
                      color: transaction.status === 'COMPLETED' ? '#155724' : '#856404',
                      padding: '0.25rem 0.5rem',
                      borderRadius: '4px',
                      fontSize: '0.85rem'
                    }}>
                      {transaction.status}
                    </span>
                  </td>
                  <td style={{ padding: '1rem', color: '#6c757d' }}>
                    {transaction.description || '-'}
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}
