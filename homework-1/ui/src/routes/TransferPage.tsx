import { useState, useEffect, FormEvent } from 'react';
import { apiClient } from '../api/apiClient';
import { Account } from '../api/types';
import { Layout } from '../components/Layout';
import { ProtectedRoute } from '../components/ProtectedRoute';

export function TransferPage() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [sourceCardNumber, setSourceCardNumber] = useState('');
  const [targetCardNumber, setTargetCardNumber] = useState('');
  const [sourceCurrency, setSourceCurrency] = useState('EUR');
  const [sourceAmount, setSourceAmount] = useState('');
  const [targetCurrency, setTargetCurrency] = useState('USD');
  const [fxRate, setFxRate] = useState('1.0');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadAccounts();
  }, []);

  const loadAccounts = async () => {
    try {
      const data = await apiClient.getAccounts();
      setAccounts(data.accounts);

      // Auto-select first account if available
      if (data.accounts.length > 0) {
        setSourceCardNumber(data.accounts[0].card_number);
        setSourceCurrency(data.accounts[0].currency);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load accounts');
    }
  };

  const handleSourceAccountChange = (cardNumber: string) => {
    setSourceCardNumber(cardNumber);
    const account = accounts.find(acc => acc.card_number === cardNumber);
    if (account) {
      setSourceCurrency(account.currency);
    }
  };

  const calculateTargetAmount = (): string => {
    if (!sourceAmount || !fxRate) return '0.00';
    const result = parseFloat(sourceAmount) * parseFloat(fxRate);
    return result.toFixed(2);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const response = await apiClient.transfer({
        source_card_number: sourceCardNumber,
        target_card_number: targetCardNumber,
        source_currency: sourceCurrency,
        source_amount: sourceAmount,
        target_currency: targetCurrency,
        fx_rate: fxRate,
        description: description || undefined
      });

      setSuccess(
        `Transfer successful! Transaction ID: ${response.transaction_id}. ` +
        `New source balance: ${response.source_account.balance} ${response.source_account.currency}`
      );

      // Reset form
      setSourceAmount('');
      setTargetCardNumber('');
      setDescription('');

      // Reload accounts to update balances
      await loadAccounts();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Transfer failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ProtectedRoute>
      <Layout>
        <div style={{ maxWidth: '600px', margin: '0 auto' }}>
          <h2 style={{ marginBottom: '1.5rem' }}>Make a Transfer</h2>

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

          {success && (
            <div style={{
              backgroundColor: '#d4edda',
              color: '#155724',
              padding: '0.75rem',
              borderRadius: '4px',
              marginBottom: '1rem'
            }}>
              {success}
            </div>
          )}

          <div style={{
            backgroundColor: 'white',
            padding: '2rem',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <form onSubmit={handleSubmit}>
              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                  Source Account
                </label>
                <select
                  value={sourceCardNumber}
                  onChange={(e) => handleSourceAccountChange(e.target.value)}
                  required
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    fontSize: '1rem',
                    border: '1px solid #ddd',
                    borderRadius: '4px'
                  }}
                >
                  <option value="">Select account</option>
                  {accounts.map((account) => (
                    <option key={account.id} value={account.card_number}>
                      {account.card_number} - {account.currency} - Balance: {account.balance}
                    </option>
                  ))}
                </select>
              </div>

              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                  Target Card Number
                </label>
                <input
                  type="text"
                  value={targetCardNumber}
                  onChange={(e) => setTargetCardNumber(e.target.value)}
                  required
                  placeholder="1111 2222 3333 4444"
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

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.5rem' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                    Source Amount
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={sourceAmount}
                    onChange={(e) => setSourceAmount(e.target.value)}
                    required
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
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                    Source Currency
                  </label>
                  <input
                    type="text"
                    value={sourceCurrency}
                    readOnly
                    style={{
                      width: '100%',
                      padding: '0.5rem',
                      fontSize: '1rem',
                      border: '1px solid #ddd',
                      borderRadius: '4px',
                      backgroundColor: '#e9ecef',
                      boxSizing: 'border-box'
                    }}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.5rem' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                    Exchange Rate (Course)
                  </label>
                  <input
                    type="number"
                    step="0.00000001"
                    min="0.00000001"
                    value={fxRate}
                    onChange={(e) => setFxRate(e.target.value)}
                    required
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
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                    Target Currency
                  </label>
                  <select
                    value={targetCurrency}
                    onChange={(e) => setTargetCurrency(e.target.value)}
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
              </div>

              <div style={{
                backgroundColor: '#e7f3ff',
                padding: '1rem',
                borderRadius: '4px',
                marginBottom: '1.5rem'
              }}>
                <div style={{ fontWeight: 'bold', marginBottom: '0.5rem' }}>
                  Calculated Target Amount:
                </div>
                <div style={{ fontSize: '1.5rem', color: '#2c3e50' }}>
                  {calculateTargetAmount()} {targetCurrency}
                </div>
              </div>

              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                  Description (Optional)
                </label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  maxLength={500}
                  rows={3}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    fontSize: '1rem',
                    border: '1px solid #ddd',
                    borderRadius: '4px',
                    boxSizing: 'border-box',
                    fontFamily: 'inherit'
                  }}
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                style={{
                  width: '100%',
                  backgroundColor: '#27ae60',
                  color: 'white',
                  border: 'none',
                  padding: '0.75rem',
                  fontSize: '1rem',
                  borderRadius: '4px',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  opacity: loading ? 0.6 : 1
                }}
              >
                {loading ? 'Processing Transfer...' : 'Transfer'}
              </button>
            </form>
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  );
}
