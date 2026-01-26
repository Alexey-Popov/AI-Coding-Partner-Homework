import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import { Account, Transaction } from '../api/types';
import { Layout } from '../components/Layout';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { TransactionsTable } from '../components/TransactionsTable';

export function AccountPage() {
  const { accountId } = useParams<{ accountId: string }>();
  const navigate = useNavigate();
  const [account, setAccount] = useState<Account | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [total, setTotal] = useState(0);
  const [limit] = useState(20);
  const [offset, setOffset] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (accountId) {
      loadData();
    }
  }, [accountId, offset]);

  const loadData = async () => {
    if (!accountId) return;

    setLoading(true);
    setError('');

    try {
      const [accountData, transactionsData] = await Promise.all([
        apiClient.getAccount(accountId),
        apiClient.getAccountTransactions(accountId, limit, offset)
      ]);

      setAccount(accountData);
      setTransactions(transactionsData.transactions);
      setTotal(transactionsData.total);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handlePrevPage = () => {
    if (offset >= limit) {
      setOffset(offset - limit);
    }
  };

  const handleNextPage = () => {
    if (offset + limit < total) {
      setOffset(offset + limit);
    }
  };

  if (loading) {
    return (
      <ProtectedRoute>
        <Layout>
          <div style={{ textAlign: 'center', padding: '2rem' }}>Loading...</div>
        </Layout>
      </ProtectedRoute>
    );
  }

  if (error || !account) {
    return (
      <ProtectedRoute>
        <Layout>
          <div style={{
            backgroundColor: '#f8d7da',
            color: '#721c24',
            padding: '0.75rem',
            borderRadius: '4px',
            marginBottom: '1rem'
          }}>
            {error || 'Account not found'}
          </div>
          <button
            onClick={() => navigate('/dashboard')}
            style={{
              backgroundColor: '#3498db',
              color: 'white',
              border: 'none',
              padding: '0.5rem 1rem',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Back to Dashboard
          </button>
        </Layout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div>
          <button
            onClick={() => navigate('/dashboard')}
            style={{
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              padding: '0.5rem 1rem',
              borderRadius: '4px',
              cursor: 'pointer',
              marginBottom: '1rem'
            }}
          >
            ← Back to Dashboard
          </button>

          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '8px',
            marginBottom: '1.5rem',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <h2 style={{ marginTop: 0, marginBottom: '1rem' }}>Account Details</h2>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem' }}>
              <div>
                <div style={{ color: '#6c757d', marginBottom: '0.25rem' }}>Currency</div>
                <div style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>{account.currency}</div>
              </div>
              <div>
                <div style={{ color: '#6c757d', marginBottom: '0.25rem' }}>Balance</div>
                <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#27ae60' }}>
                  {parseFloat(account.balance).toFixed(2)}
                </div>
              </div>
              <div>
                <div style={{ color: '#6c757d', marginBottom: '0.25rem' }}>Card Number</div>
                <div style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>{account.card_number}</div>
              </div>
            </div>
          </div>

          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <h3 style={{ marginTop: 0, marginBottom: '1rem' }}>Transactions</h3>

            {accountId && <TransactionsTable transactions={transactions} currentAccountId={accountId} />}

            {total > limit && (
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1rem', alignItems: 'center' }}>
                <button
                  onClick={handlePrevPage}
                  disabled={offset === 0}
                  style={{
                    backgroundColor: '#3498db',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '4px',
                    cursor: offset === 0 ? 'not-allowed' : 'pointer',
                    opacity: offset === 0 ? 0.5 : 1
                  }}
                >
                  Previous
                </button>
                <div style={{ color: '#6c757d' }}>
                  Showing {offset + 1} - {Math.min(offset + limit, total)} of {total}
                </div>
                <button
                  onClick={handleNextPage}
                  disabled={offset + limit >= total}
                  style={{
                    backgroundColor: '#3498db',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '4px',
                    cursor: offset + limit >= total ? 'not-allowed' : 'pointer',
                    opacity: offset + limit >= total ? 0.5 : 1
                  }}
                >
                  Next
                </button>
              </div>
            )}
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  );
}
