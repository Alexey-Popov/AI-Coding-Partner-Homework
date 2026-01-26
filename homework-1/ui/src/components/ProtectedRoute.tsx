import { ReactNode, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authStore } from '../auth/authStore';

interface ProtectedRouteProps {
  children: ReactNode;
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const navigate = useNavigate();

  useEffect(() => {
    if (!authStore.isAuthenticated()) {
      navigate('/login');
    }
  }, [navigate]);

  if (!authStore.isAuthenticated()) {
    return null;
  }

  return <>{children}</>;
}
