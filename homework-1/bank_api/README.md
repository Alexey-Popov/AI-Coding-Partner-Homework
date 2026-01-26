# Banking Transactions API

Production-ready Banking Transactions API built with FastAPI, clean layered architecture, and H2 file-based database.

## Features

- **Authentication**: JWT-based authentication with bcrypt password hashing
- **Role-Based Access Control (RBAC)**: USER and ADMIN roles with fine-grained permissions
- **Accounts**: Create and manage multiple accounts with different currencies
- **Transfers**: Execute transfers between accounts with currency conversion
- **Transactions**: Track all financial transactions with pagination
- **Admin UI**: Manage users, block/unblock accounts, view all transactions
- **Clean Architecture**: Layered design (API → Service → Domain → Repository → Infrastructure)
- **OpenAPI/Swagger**: Auto-generated API documentation at `/docs`
- **Production-Ready**: Proper error handling, validation, logging, and security

## Tech Stack

- **Python 3.11+**
- **FastAPI** - Modern web framework
- **H2 Database** - File-based SQL database (via JDBC)
- **JayDeBeApi + JPype** - Python-Java bridge for H2 connectivity
- **JWT** - Secure authentication tokens
- **Pydantic** - Data validation and serialization
- **Pytest** - Testing framework

## Prerequisites

1. **Python 3.11+**
   ```bash
   python --version
   ```

2. **Java Runtime Environment (JRE)**
   ```bash
   java -version
   ```

3. **H2 Database JAR**
   Download the H2 database JAR file:
   ```bash
   curl -o h2.jar https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar
   ```

   Place `h2.jar` in the project root directory or specify the path in `.env`

## Installation

1. **Clone the repository**
   ```bash
   cd bank_api
   ```

2. **Create virtual environment**
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Configure environment**
   ```bash
   cp .env.example .env
   ```

   Edit `.env` and set:
   - `JWT_SECRET`: Your secret key for JWT tokens
   - `H2_JAR_PATH`: Path to h2.jar file (default: `./h2.jar`)
   - Other settings as needed

## Running the API

1. **Start the server**
   ```bash
   uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```

2. **Access the API**
   - API: http://localhost:8000
   - Swagger UI: http://localhost:8000/docs
   - OpenAPI JSON: http://localhost:8000/openapi.json
   - Health Check: http://localhost:8000/health

## Role-Based Access Control (RBAC)

### User Roles

- **USER** (default): Standard user with access to their own accounts and transactions
- **ADMIN**: Administrator with access to user management and system-wide operations

### User Status

- **ACTIVE** (default): User can login and access protected endpoints
- **BLOCKED**: User cannot login or access any protected endpoints

### Creating an Admin User

Admin users can be created automatically on application startup using environment variables:

```bash
# Set in .env file
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=secure_admin_password
```

On startup, if these variables are set and the admin user doesn't exist, it will be created automatically.

### Admin Capabilities

Admins have access to:
- List all users with filters (status, email)
- Block/unblock user accounts
- View all transactions across all users
- Filter transactions by user, account, type, status, date range

### Blocked User Behavior

When a user is blocked:
- Cannot login (returns 403 Forbidden with USER_BLOCKED error code)
- Existing JWT tokens are denied at protected endpoints
- Cannot execute transfers or access any account operations

## API Documentation

### Authentication Endpoints

#### Register
```bash
curl -X POST http://localhost:8000/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "full_name": "John Doe"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8000/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

Response:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer"
}
```

#### Get Current User
```bash
curl -X GET http://localhost:8000/v1/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Account Endpoints

#### Create Account
```bash
curl -X POST http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "EUR",
    "initial_balance": "1000.00"
  }'
```

#### Get User Accounts
```bash
curl -X GET http://localhost:8000/v1/accounts \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get Account Details
```bash
curl -X GET http://localhost:8000/v1/accounts/{account_id} \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Get Account Transactions
```bash
curl -X GET "http://localhost:8000/v1/accounts/{account_id}/transactions?limit=20&offset=0" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Transfer Endpoints

#### Execute Transfer
```bash
curl -X POST http://localhost:8000/v1/transfers \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "source_card_number": "1111 2222 3333 4444",
    "target_card_number": "5555 6666 7777 8888",
    "source_currency": "EUR",
    "source_amount": "100.00",
    "target_currency": "USD",
    "fx_rate": "1.1",
    "description": "Payment for services"
  }'
```

Response:
```json
{
  "transaction_id": "uuid",
  "status": "COMPLETED",
  "source_account": {
    "id": "uuid",
    "balance": "900.00",
    ...
  },
  "target_account": {
    "id": "uuid",
    "balance": "110.00",
    ...
  }
}
```

### Admin Endpoints (ADMIN Role Required)

#### List All Users
```bash
curl -X GET "http://localhost:8000/v1/admin/users?limit=20&offset=0&status=ACTIVE" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

Response:
```json
{
  "users": [
    {
      "id": "uuid",
      "email": "user@example.com",
      "full_name": "John Doe",
      "role": "USER",
      "status": "ACTIVE",
      "created_at": "2024-01-01T00:00:00",
      "updated_at": "2024-01-01T00:00:00"
    }
  ],
  "total": 1,
  "limit": 20,
  "offset": 0
}
```

#### Block User
```bash
curl -X PATCH http://localhost:8000/v1/admin/users/{user_id}/block \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

Response:
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "status": "BLOCKED",
  ...
}
```

#### Unblock User
```bash
curl -X PATCH http://localhost:8000/v1/admin/users/{user_id}/unblock \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

#### List All Transactions
```bash
curl -X GET "http://localhost:8000/v1/admin/transactions?limit=20&offset=0&type=TRANSFER&status=COMPLETED" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

Response:
```json
{
  "transactions": [
    {
      "id": "uuid",
      "created_at": "2024-01-01T00:00:00",
      "type": "TRANSFER",
      "status": "COMPLETED",
      "source_user_id": "uuid",
      "target_user_id": "uuid",
      "source_card_number": "1111 2222 3333 4444",
      "target_card_number": "5555 6666 7777 8888",
      "source_amount": 100.0,
      "source_currency": "EUR",
      "target_amount": 110.0,
      "target_currency": "USD",
      ...
    }
  ],
  "total": 1,
  "limit": 20,
  "offset": 0
}
```

## Testing

Run the test suite:

```bash
pytest tests/ -v
```

Run specific test files:
```bash
pytest tests/test_auth.py -v
pytest tests/test_transfers.py -v
```

## Database

The H2 database is stored as a file at `./bank.db` (creates `bank.db.mv.db` file).

### Schema

The database schema includes:
- `users` - User accounts with hashed passwords
- `accounts` - Bank accounts/cards with balances
- `transactions` - All financial transactions
- `idempotency_keys` - Idempotency tracking for transfers

### Migrations

Migrations run automatically on application startup. Tables are created if they don't exist.

## Error Handling

The API uses standard HTTP status codes:

- `200` - Success
- `201` - Created
- `400` - Bad Request (validation error)
- `401` - Unauthorized (invalid/missing token)
- `403` - Forbidden (access denied)
- `404` - Not Found
- `409` - Conflict (duplicate email/card, idempotency)
- `422` - Unprocessable Entity (insufficient funds)
- `500` - Internal Server Error

Error response format:
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Error description",
    "details": {}
  },
  "request_id": "uuid"
}
```

## Security Features

- **Password Hashing**: Bcrypt with salt
- **JWT Tokens**: Short-lived access tokens (15 min default)
- **Input Validation**: Pydantic models validate all inputs
- **SQL Injection Protection**: Parameterized queries
- **CORS**: Configurable allowed origins
- **Request IDs**: Correlation tracking for debugging

## Architecture

```
app/
├── api/                 # API layer (controllers/routers)
│   ├── dependencies.py  # FastAPI dependencies (auth, etc.)
│   ├── schemas.py       # Pydantic request/response models
│   └── v1/              # API version 1
│       ├── auth.py
│       ├── accounts.py
│       └── transfers.py
├── services/            # Business logic layer
│   ├── auth_service.py
│   ├── account_service.py
│   └── transfer_service.py
├── domain/              # Domain models and logic
│   ├── models.py        # Domain entities
│   └── money.py         # Money calculations
├── repositories/        # Data access layer
│   ├── user_repo.py
│   ├── account_repo.py
│   ├── transaction_repo.py
│   └── idempotency_repo.py
├── infrastructure/      # Infrastructure concerns
│   ├── db.py            # Database connection
│   ├── security.py      # JWT, password hashing
│   ├── settings.py      # Configuration
│   ├── migrations.py    # Schema migrations
│   └── logging.py       # Structured logging
└── main.py              # Application entry point
```

## Development

### Code Style

- Full type hints
- Pydantic models for validation
- Decimal for money amounts (no floats)
- Structured logging with request IDs
- Clean separation of concerns

### Adding New Features

1. Define domain models in `domain/models.py`
2. Create repository in `repositories/`
3. Implement business logic in `services/`
4. Add API endpoints in `api/v1/`
5. Add Pydantic schemas in `api/schemas.py`
6. Write tests in `tests/`

## Troubleshooting

### JVM Issues

If you get JVM-related errors:
```bash
# Check Java is installed
java -version

# Verify H2 JAR path in .env
H2_JAR_PATH=./h2.jar
```

### Database Connection Issues

If database connection fails:
1. Ensure H2 JAR is downloaded and path is correct
2. Check file permissions for database directory
3. Verify DB_URL in .env is correct

### Port Already in Use

If port 8000 is in use:
```bash
# Use a different port
uvicorn app.main:app --port 8001
```

## License

MIT License
