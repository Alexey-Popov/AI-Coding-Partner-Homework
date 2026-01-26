import { useState, useEffect, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import { Account, User } from '../api/types';
import { Layout } from '../components/Layout';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { AccountCard } from '../components/AccountCard';

export function DashboardPage() {
  const [user, setUser] = useState<User | null>(null);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [currency, setCurrency] = useState('EUR');
  const [initialBalance, setInitialBalance] = useState('0');
  const [creating, setCreating] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError('');

    try {
      const [userData, accountsData] = await Promise.all([
        apiClient.getCurrentUser(),
        apiClient.getAccounts()
      ]);

      setUser(userData);
      setAccounts(accountsData.accounts);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAccount = async (e: FormEvent) => {
    e.preventDefault();
    setCreating(true);
    setError('');

    try {
      await apiClient.createAccount({
        currency: currency.toUpperCase(),
        initial_balance: initialBalance
      });

      setShowCreateForm(false);
      setCurrency('EUR');
      setInitialBalance('0');
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create account');
    } finally {
      setCreating(false);
    }
  };

  return (
    <ProtectedRoute>
      <Layout>
        <div>
          <h2 style={{ marginBottom: '1rem' }}>Dashboard</h2>

          {user && (
            <div style={{
              backgroundColor: 'white',
              padding: '1rem',
              borderRadius: '8px',
              marginBottom: '1.5rem',
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
            }}>
              <p style={{ margin: 0 }}>
                <strong>Welcome, {user.full_name || user.email}!</strong>
              </p>
            </div>
          )}

          {error && (
            <div style={{
              backgroundColor: '#f8d7da',
              color: '#721c24',
              padding: '0.75rem',
              borderRadius: '4px',
              marginBottom: '1rem'
            }}>
              {error}
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ margin: 0 }}>Your Accounts</h3>
            <button
              onClick={() => setShowCreateForm(!showCreateForm)}
              style={{
                backgroundColor: '#27ae60',
                color: 'white',
                border: 'none',
                padding: '0.5rem 1rem',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              {showCreateForm ? 'Cancel' : '+ New Account'}
            </button>
          </div>

          {showCreateForm && (
            <div style={{
              backgroundColor: 'white',
              padding: '1.5rem',
              borderRadius: '8px',
              marginBottom: '1.5rem',
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
            }}>
              <h4 style={{ marginTop: 0 }}>Create New Account</h4>
              <form onSubmit={handleCreateAccount}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                      Currency
                    </label>
                    <select
                      value={currency}
                      onChange={(e) => setCurrency(e.target.value)}
                      style={{
                        width: '100%',
                        padding: '0.5rem',
                        fontSize: '1rem',
                        border: '1px solid #ddd',
                        borderRadius: '4px'
                      }}
                    >
                      <option value="EUR">EUR</option>
                      <option value="USD">USD</option>
                      <option value="GBP">GBP</option>
                      <option value="JPY">JPY</option>
                    </select>
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                      Initial Balance
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      value={initialBalance}
                      onChange={(e) => setInitialBalance(e.target.value)}
                      style={{
                        width: '100%',
                        padding: '0.5rem',
                        fontSize: '1rem',
                        border: '1px solid #ddd',
                        borderRadius: '4px',
                        boxSizing: 'border-box'
                      }}
                    />
                  </div>
                </div>
                <button
                  type="submit"
                  disabled={creating}
                  style={{
                    backgroundColor: '#3498db',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '4px',
                    cursor: creating ? 'not-allowed' : 'pointer',
                    opacity: creating ? 0.6 : 1
                  }}
                >
                  {creating ? 'Creating...' : 'Create Account'}
                </button>
              </form>
            </div>
          )}

          {loading ? (
            <div style={{ textAlign: 'center', padding: '2rem' }}>Loading...</div>
          ) : accounts.length === 0 ? (
            <div style={{
              backgroundColor: 'white',
              padding: '2rem',
              borderRadius: '8px',
              textAlign: 'center',
              color: '#6c757d'
            }}>
              No accounts yet. Create your first account to get started!
            </div>
          ) : (
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
              gap: '1rem'
            }}>
              {accounts.map((account) => (
                <AccountCard
                  key={account.id}
                  account={account}
                  onViewTransactions={() => navigate(`/accounts/${account.id}`)}
                />
              ))}
            </div>
          )}
        </div>
      </Layout>
    </ProtectedRoute>
  );
}
