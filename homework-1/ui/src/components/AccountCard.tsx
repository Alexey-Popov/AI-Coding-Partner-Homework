import { Account } from '../api/types';

interface AccountCardProps {
  account: Account;
  onViewTransactions: () => void;
}

export function AccountCard({ account, onViewTransactions }: AccountCardProps) {
  return (
    <div style={{
      backgroundColor: 'white',
      borderRadius: '8px',
      padding: '1.5rem',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
      marginBottom: '1rem'
    }}>
      <div style={{ marginBottom: '1rem' }}>
        <div style={{ fontSize: '1.2rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>
          {account.currency}
        </div>
        <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#2c3e50' }}>
          {parseFloat(account.balance).toFixed(2)}
        </div>
      </div>
      <div style={{ marginBottom: '1rem', color: '#7f8c8d' }}>
        {account.card_number}
      </div>
      <button
        onClick={onViewTransactions}
        style={{
          backgroundColor: '#3498db',
          color: 'white',
          border: 'none',
          padding: '0.5rem 1rem',
          borderRadius: '4px',
          cursor: 'pointer',
          fontSize: '0.9rem'
        }}
      >
        View Transactions
      </button>
    </div>
  );
}
