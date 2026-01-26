import { useState, useEffect } from 'react';
import { Layout } from '../../components/Layout';
import { AdminRoute } from '../../components/AdminRoute';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { apiClient } from '../../api/apiClient';
import { AdminUserSummary } from '../../api/types';

export function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUserSummary[]>([]);
  const [total, setTotal] = useState(0);
  const [limit] = useState(20);
  const [offset, setOffset] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [emailFilter, setEmailFilter] = useState('');

  // Confirm dialog state
  const [confirmDialog, setConfirmDialog] = useState<{
    isOpen: boolean;
    userId: string;
    action: 'block' | 'unblock';
    userEmail: string;
  }>({
    isOpen: false,
    userId: '',
    action: 'block',
    userEmail: ''
  });

  useEffect(() => {
    loadUsers();
  }, [offset, statusFilter]);

  const loadUsers = async () => {
    setLoading(true);
    setError('');

    try {
      const data = await apiClient.adminListUsers(
        limit,
        offset,
        statusFilter || undefined,
        emailFilter || undefined
      );

      setUsers(data.users);
      setTotal(data.total);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  const handleBlockUser = (userId: string, email: string) => {
    setConfirmDialog({
      isOpen: true,
      userId,
      action: 'block',
      userEmail: email
    });
  };

  const handleUnblockUser = (userId: string, email: string) => {
    setConfirmDialog({
      isOpen: true,
      userId,
      action: 'unblock',
      userEmail: email
    });
  };

  const confirmAction = async () => {
    const { userId, action } = confirmDialog;
    setConfirmDialog({ ...confirmDialog, isOpen: false });
    setError('');
    setSuccess('');

    try {
      if (action === 'block') {
        await apiClient.adminBlockUser(userId);
        setSuccess('User blocked successfully');
      } else {
        await apiClient.adminUnblockUser(userId);
        setSuccess('User unblocked successfully');
      }

      // Reload users
      await loadUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : `Failed to ${action} user`);
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
    loadUsers();
  };

  return (
    <AdminRoute>
      <Layout>
        <div>
          <h2 style={{ marginBottom: '1.5rem' }}>User Management</h2>

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
            padding: '1.5rem',
            borderRadius: '8px',
            marginBottom: '1.5rem',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <h3 style={{ marginTop: 0, marginBottom: '1rem' }}>Filters</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
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
                  <option value="ACTIVE">Active</option>
                  <option value="BLOCKED">Blocked</option>
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Email</label>
                <input
                  type="text"
                  value={emailFilter}
                  onChange={(e) => setEmailFilter(e.target.value)}
                  placeholder="Search by email..."
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
            overflow: 'hidden'
          }}>
            {loading ? (
              <div style={{ padding: '2rem', textAlign: 'center' }}>Loading...</div>
            ) : (
              <>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ backgroundColor: '#f8f9fa', borderBottom: '2px solid #dee2e6' }}>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Email</th>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Name</th>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Role</th>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Status</th>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Created</th>
                      <th style={{ padding: '1rem', textAlign: 'left' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((user) => (
                      <tr key={user.id} style={{ borderBottom: '1px solid #dee2e6' }}>
                        <td style={{ padding: '1rem' }}>{user.email}</td>
                        <td style={{ padding: '1rem' }}>{user.full_name || '-'}</td>
                        <td style={{ padding: '1rem' }}>
                          <span style={{
                            padding: '0.25rem 0.5rem',
                            borderRadius: '4px',
                            backgroundColor: user.role === 'ADMIN' ? '#e3f2fd' : '#f5f5f5',
                            color: user.role === 'ADMIN' ? '#1976d2' : '#666'
                          }}>
                            {user.role}
                          </span>
                        </td>
                        <td style={{ padding: '1rem' }}>
                          <span style={{
                            padding: '0.25rem 0.5rem',
                            borderRadius: '4px',
                            backgroundColor: user.status === 'ACTIVE' ? '#e8f5e9' : '#ffebee',
                            color: user.status === 'ACTIVE' ? '#2e7d32' : '#c62828'
                          }}>
                            {user.status}
                          </span>
                        </td>
                        <td style={{ padding: '1rem' }}>
                          {new Date(user.created_at).toLocaleDateString()}
                        </td>
                        <td style={{ padding: '1rem' }}>
                          {user.status === 'ACTIVE' ? (
                            <button
                              onClick={() => handleBlockUser(user.id, user.email)}
                              style={{
                                padding: '0.25rem 0.75rem',
                                backgroundColor: '#e74c3c',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '0.875rem'
                              }}
                            >
                              Block
                            </button>
                          ) : (
                            <button
                              onClick={() => handleUnblockUser(user.id, user.email)}
                              style={{
                                padding: '0.25rem 0.75rem',
                                backgroundColor: '#27ae60',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '0.875rem'
                              }}
                            >
                              Unblock
                            </button>
                          )}
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

          <ConfirmDialog
            isOpen={confirmDialog.isOpen}
            title={`${confirmDialog.action === 'block' ? 'Block' : 'Unblock'} User`}
            message={`Are you sure you want to ${confirmDialog.action} user ${confirmDialog.userEmail}?`}
            onConfirm={confirmAction}
            onCancel={() => setConfirmDialog({ ...confirmDialog, isOpen: false })}
            confirmText={confirmDialog.action === 'block' ? 'Block' : 'Unblock'}
          />
        </div>
      </Layout>
    </AdminRoute>
  );
}
