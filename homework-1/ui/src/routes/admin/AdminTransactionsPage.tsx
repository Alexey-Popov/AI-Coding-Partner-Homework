import { useState, useEffect } from 'react';
import { Layout } from '../../components/Layout';
import { AdminRoute } from '../../components/AdminRoute';
import { apiClient } from '../../api/apiClient';
import { AdminTransactionSummary } from '../../api/types';

export function AdminTransactionsPage() {
  const [transactions, setTransactions] = useState<AdminTransactionSummary[]>([]);
  const [total, setTotal] = useState(0);
  const [limit] = useState(20);
  const [offset, setOffset] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Filters
  const [typeFilter, setTypeFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [userIdFilter, setUserIdFilter] = useState('');

  useEffect(() => {
    loadTransactions();
  }, [offset]);

  const loadTransactions = async () => {
    setLoading(true);
    setError('');

    try {
      const filters: any = {};
      if (typeFilter) filters.type = typeFilter;
      if (statusFilter) filters.status = statusFilter;
      if (userIdFilter) filters.user_id = userIdFilter;

      const data = await apiClient.adminListTransactions(limit, offset, filters);

      setTransactions(data.transactions);
      setTotal(data.total);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load transactions');
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

  const handleSearch = () => {
    setOffset(0);
    loadTransactions();
  };

  return (
    <AdminRoute>
      <Layout>
        <div>
          <h2 style={{ marginBottom: '1.5rem' }}>All Transactions</h2>

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

          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '8px',
            marginBottom: '1.5rem',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <h3 style={{ marginTop: 0, marginBottom: '1rem' }}>Filters</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Type</label>
                <select
                  value={typeFilter}
                  onChange={(e) => setTypeFilter(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #ddd',
                    borderRadius: '4px'
                  }}
                >
                  <option value="">All</option>
                  <option value="DEBIT">Debit</option>
                  <option value="CREDIT">Credit</option>
                  <option value="TRANSFER">Transfer</option>
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Status</label>
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #ddd',
                    borderRadius: '4px'
                  }}
                >
                  <option value="">All</option>
                  <option value="PENDING">Pending</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="FAILED">Failed</option>
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>User ID</label>
                <input
                  type="text"
                  value={userIdFilter}
                  onChange={(e) => setUserIdFilter(e.target.value)}
                  placeholder="Filter by user ID..."
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #ddd',
                    borderRadius: '4px'
                  }}
                />
              </div>
            </div>
            <button
              onClick={handleSearch}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#3498db',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Search
            </button>
          </div>

          <div style={{
            backgroundColor: 'white',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            overflow: 'auto'
          }}>
            {loading ? (
              <div style={{ padding: '2rem', textAlign: 'center' }}>Loading...</div>
            ) : (
              <>
                <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '1000px' }}>
                  <thead>
                    <tr style={{ backgroundColor: '#f8f9fa', borderBottom: '2px solid #dee2e6' }}>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Date</th>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Type</th>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Status</th>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Source</th>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Target</th>
                      <th style={{ padding: '1rem', textAlign: 'right' }}>Amount</th>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.map((txn) => (
                      <tr key={txn.id} style={{ borderBottom: '1px solid #dee2e6' }}>
                        <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                          {new Date(txn.created_at).toLocaleString()}
                        </td>
                        <td style={{ padding: '1rem' }}>
                          <span style={{
                            padding: '0.25rem 0.5rem',
                            borderRadius: '4px',
                            backgroundColor: '#f5f5f5',
                            fontSize: '0.75rem'
                          }}>
                            {txn.type}
                          </span>
                        </td>
                        <td style={{ padding: '1rem' }}>
                          <span style={{
                            padding: '0.25rem 0.5rem',
                            borderRadius: '4px',
                            backgroundColor: txn.status === 'COMPLETED' ? '#e8f5e9' : txn.status === 'FAILED' ? '#ffebee' : '#fff3e0',
                            color: txn.status === 'COMPLETED' ? '#2e7d32' : txn.status === 'FAILED' ? '#c62828' : '#f57c00',
                            fontSize: '0.75rem'
                          }}>
                            {txn.status}
                          </span>
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                          {txn.source_card_number || '-'}
                          {txn.source_user_id && (
                            <div style={{ color: '#666', fontSize: '0.75rem' }}>
                              User: {txn.source_user_id.substring(0, 8)}...
                            </div>
                          )}
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                          {txn.target_card_number || '-'}
                          {txn.target_user_id && (
                            <div style={{ color: '#666', fontSize: '0.75rem' }}>
                              User: {txn.target_user_id.substring(0, 8)}...
                            </div>
                          )}
                        </td>
                        <td style={{ padding: '1rem', textAlign: 'right', fontSize: '0.875rem' }}>
                          {txn.source_amount && txn.source_currency && (
                            <div>{txn.source_amount.toFixed(2)} {txn.source_currency}</div>
                          )}
                          {txn.target_amount && txn.target_currency && (
                            <div style={{ color: '#27ae60' }}>
                              {txn.target_amount.toFixed(2)} {txn.target_currency}
                            </div>
                          )}
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.875rem', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {txn.description || '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {total > limit && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '1rem', alignItems: 'center', borderTop: '1px solid #dee2e6' }}>
                    <button
                      onClick={handlePrevPage}
                      disabled={offset === 0}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: offset === 0 ? '#ddd' : '#3498db',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: offset === 0 ? 'not-allowed' : 'pointer'
                      }}
                    >
                      Previous
                    </button>
                    <div style={{ color: '#666' }}>
                      Showing {offset + 1} - {Math.min(offset + limit, total)} of {total}
                    </div>
                    <button
                      onClick={handleNextPage}
                      disabled={offset + limit >= total}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: offset + limit >= total ? '#ddd' : '#3498db',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: offset + limit >= total ? 'not-allowed' : 'pointer'
                      }}
                    >
                      Next
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </Layout>
    </AdminRoute>
  );
}
