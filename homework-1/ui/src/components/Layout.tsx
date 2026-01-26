import { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { authStore } from '../auth/authStore';

interface LayoutProps {
  children: ReactNode;
}

export function Layout({ children }: LayoutProps) {
  const navigate = useNavigate();

  const handleLogout = () => {
    authStore.clearToken();
    navigate('/login');
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <nav style={{
        backgroundColor: '#2c3e50',
        color: 'white',
        padding: '1rem 2rem',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <h1 style={{ margin: 0, fontSize: '1.5rem' }}>Banking App</h1>
        {authStore.isAuthenticated() && (
          <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
            <button
              onClick={() => navigate('/dashboard')}
              style={{
                background: 'none',
                border: 'none',
                color: 'white',
                cursor: 'pointer',
                fontSize: '1rem'
              }}
            >
              Dashboard
            </button>
            <button
              onClick={() => navigate('/transfer')}
              style={{
                background: 'none',
                border: 'none',
                color: 'white',
                cursor: 'pointer',
                fontSize: '1rem'
              }}
            >
              Transfer
            </button>
            {authStore.isAdmin() && (
              <>
                <span style={{ color: '#95a5a6', margin: '0 0.5rem' }}>|</span>
                <button
                  onClick={() => navigate('/admin/users')}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: '#f39c12',
                    cursor: 'pointer',
                    fontSize: '1rem',
                    fontWeight: 'bold'
                  }}
                >
                  Users
                </button>
                <button
                  onClick={() => navigate('/admin/transactions')}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: '#f39c12',
                    cursor: 'pointer',
                    fontSize: '1rem',
                    fontWeight: 'bold'
                  }}
                >
                  Transactions
                </button>
              </>
            )}
            <button
              onClick={handleLogout}
              style={{
                backgroundColor: '#e74c3c',
                border: 'none',
                color: 'white',
                padding: '0.5rem 1rem',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '1rem'
              }}
            >
              Logout
            </button>
          </div>
        )}
      </nav>
      <main style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
        {children}
      </main>
    </div>
  );
}
