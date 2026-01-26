# Banking UI

Modern React-based web interface for the Banking Transactions API.

## Features

- **Authentication**: Register and login with JWT tokens
- **Dashboard**: View all accounts with balances
- **Account Management**: Create new accounts, view transactions
- **Transfers**: Execute transfers with currency conversion
- **Responsive Design**: Clean, minimal UI
- **Type-Safe**: Full TypeScript coverage

## Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **React Router** - Client-side routing

## Prerequisites

- **Node.js 18+** and npm
- **Backend API** running on http://localhost:8000

## Installation

1. **Install dependencies**
   ```bash
   cd ui
   npm install
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   ```

   Edit `.env`:
   ```
   VITE_API_BASE_URL=http://localhost:8000
   ```

## Running the UI

1. **Start development server**
   ```bash
   npm run dev
   ```

2. **Access the UI**
   - Open http://localhost:5173 in your browser

## Building for Production

```bash
# Build static files
npm run build

# Preview production build
npm run preview
```

The build output will be in `dist/` directory.

## Application Flow

### 1. Register / Login

- Navigate to `/register` to create a new account
- Or `/login` to authenticate with existing credentials
- JWT token is stored in localStorage

### 2. Dashboard

- View all your bank accounts
- Create new accounts with different currencies
- Click "View Transactions" to see account history

### 3. Account Details

- View account balance and card number
- Browse transaction history with pagination
- See incoming/outgoing transfers

### 4. Transfer

- Select source account from dropdown
- Enter target card number
- Specify amount and exchange rate
- View calculated target amount
- Execute transfer

## Project Structure

```
src/
├── api/
│   ├── apiClient.ts     # Centralized API calls
│   └── types.ts         # TypeScript types
├── auth/
│   └── authStore.ts     # Authentication state
├── components/
│   ├── Layout.tsx       # App layout with navigation
│   ├── ProtectedRoute.tsx  # Auth guard
│   ├── AccountCard.tsx     # Account display card
│   └── TransactionsTable.tsx  # Transactions table
├── routes/
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx
│   ├── DashboardPage.tsx
│   ├── AccountPage.tsx
│   └── TransferPage.tsx
├── app.tsx              # App router
├── main.tsx             # Entry point
└── index.css            # Global styles
```

## Features Detail

### Authentication

- JWT tokens stored in localStorage
- Auto-redirect to login if not authenticated
- Logout clears token and redirects

### Account Cards

- Display currency, balance, and masked card number
- Color-coded for easy identification
- Quick access to transactions

### Transactions Table

- Paginated transaction history
- Shows incoming (green) and outgoing (red) transfers
- Displays amounts, currencies, status, and descriptions

### Transfer Form

- Auto-populates source account details
- Real-time calculation of target amount
- Validation for all fields
- Success/error feedback

## API Integration

All API calls go through `apiClient.ts`:

```typescript
// Example usage
import { apiClient } from './api/apiClient';

// Login
const response = await apiClient.login({ email, password });

// Create account
const account = await apiClient.createAccount({
  currency: 'EUR',
  initial_balance: '1000.00'
});

// Execute transfer
const result = await apiClient.transfer({
  source_card_number: '1111 2222 3333 4444',
  target_card_number: '5555 6666 7777 8888',
  source_currency: 'EUR',
  source_amount: '100.00',
  target_currency: 'USD',
  fx_rate: '1.1'
});
```

## Error Handling

- API errors are caught and displayed to users
- Form validation prevents invalid submissions
- Loading states show during async operations

## Development

### Adding New Features

1. Add TypeScript types in `api/types.ts`
2. Add API methods in `api/apiClient.ts`
3. Create components in `components/`
4. Create pages in `routes/`
5. Update router in `app.tsx`

### Code Style

- TypeScript for type safety
- Functional components with hooks
- Inline styles (can be replaced with CSS-in-JS or Tailwind)
- Clear component hierarchy

## Troubleshooting

### API Connection Issues

If UI can't connect to API:
1. Verify backend is running on http://localhost:8000
2. Check CORS settings in backend allow http://localhost:5173
3. Verify `.env` file has correct `VITE_API_BASE_URL`

### Build Issues

If build fails:
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Port Already in Use

If port 5173 is in use:
```bash
# Vite will auto-increment to next available port
# Or specify a different port in vite.config.ts
```

## License

MIT License
