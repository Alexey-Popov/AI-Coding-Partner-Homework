import { ReactNode, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authStore } from '../auth/authStore';

interface AdminRouteProps {
  children: ReactNode;
}

export function AdminRoute({ children }: AdminRouteProps) {
  const navigate = useNavigate();

  useEffect(() => {
    // Check if user is authenticated
    if (!authStore.isAuthenticated()) {
      navigate('/login');
      return;
    }

    // Check if user is admin
    if (!authStore.isAdmin()) {
      console.warn('Access denied: Admin role required');
      navigate('/dashboard');
      return;
    }

    // Check if user is blocked (additional safety check)
    if (authStore.isBlocked()) {
      console.warn('Access denied: User is blocked');
      authStore.clearToken();
      navigate('/login');
      return;
    }
  }, [navigate]);

  // Only render if authenticated and admin
  if (!authStore.isAuthenticated() || !authStore.isAdmin()) {
    return null;
  }

  return <>{children}</>;
}
