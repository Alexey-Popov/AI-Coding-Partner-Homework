import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from './routes/LoginPage';
import { RegisterPage } from './routes/RegisterPage';
import { DashboardPage } from './routes/DashboardPage';
import { AccountPage } from './routes/AccountPage';
import { TransferPage } from './routes/TransferPage';
import { AdminUsersPage } from './routes/admin/AdminUsersPage';
import { AdminTransactionsPage } from './routes/admin/AdminTransactionsPage';

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/accounts/:accountId" element={<AccountPage />} />
        <Route path="/transfer" element={<TransferPage />} />
        <Route path="/admin/users" element={<AdminUsersPage />} />
        <Route path="/admin/transactions" element={<AdminTransactionsPage />} />
      </Routes>
    </BrowserRouter>
  );
}
