# ▶️ How to Run the Banking Transactions API

This guide provides step-by-step instructions to run and test the Banking Transactions API.

---

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

### Required Software

| Software | Minimum Version | How to Check | Download Link |
|----------|----------------|--------------|---------------|
| **Java JDK** | 17 or higher | `java -version` | [Download Java](https://www.oracle.com/java/technologies/downloads/) |
| **Maven** | 3.6 or higher | `mvn -version` | [Download Maven](https://maven.apache.org/download.cgi) |

### Verify Installation

```bash
# Check Java version
java -version
# Expected output: java version "17.x.x" or higher

# Check Maven version
mvn -version
# Expected output: Apache Maven 3.6.x or higher
```

---

## 🚀 Quick Start (Easiest Method)

### For macOS/Linux Users

1. **Navigate to the project directory**:
   ```bash
   cd homework-1
   ```

2. **Run the application**:
   ```bash
   ./demo/run.sh
   ```

3. **Wait for the application to start**. You should see:
   ```
   🚀 Building and Running Banking Transactions API...
   ✅ Build successful!
   🏃 Starting the application...
   The API will be available at: http://localhost:8080
   ```

4. **Test the API** (in a new terminal):
   ```bash
   curl http://localhost:8080/api/transactions
   ```

### For Windows Users

1. **Navigate to the project directory**:
   ```cmd
   cd homework-1
   ```

2. **Run the application**:
   ```cmd
   demo\run.bat
   ```

3. **Wait for the application to start**

4. **Test the API** (in a new terminal):
   ```cmd
   curl http://localhost:8080/api/transactions
   ```

---

## 📝 Manual Steps (Alternative Method)

If you prefer to run commands manually:

### Step 1: Navigate to Project Directory

```bash
cd homework-1
```

### Step 2: Build the Project

```bash
mvn clean install
```

This will:
- Download all dependencies
- Compile the source code
- Run all tests
- Package the application

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

### Step 3: Run the Application

```bash
mvn spring-boot:run
```

Expected output:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.1)

...
Started BankingApiApplication in X.XXX seconds
```

### Step 4: Verify the Application is Running

Open a new terminal and run:

```bash
curl http://localhost:8080/api/transactions
```

Expected response:
```json
[]
```
(Empty array initially, as no transactions have been created yet)

---

## 🧪 Running Tests

### Quick Method

**macOS/Linux**:
```bash
./demo/test.sh
```

**Windows**:
```cmd
demo\test.bat
```

### Manual Method

```bash
mvn clean test
```

Expected output:
```
[INFO] Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 🔍 Testing the API

### Option 1: Using curl (Command Line)

Run the sample requests script:

**macOS/Linux**:
```bash
./demo/sample-requests.sh
```

Note: Requires `jq` for JSON formatting. Install with:
- macOS: `brew install jq`
- Linux: `sudo apt-get install jq`

**Manual curl commands**:

```bash
# Create a transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "TRANSFER"
  }'

# Get all transactions
curl http://localhost:8080/api/transactions

# Get account balance
curl http://localhost:8080/api/accounts/ACC-12345/balance

# Get account summary
curl http://localhost:8080/api/accounts/ACC-12345/summary

# Calculate interest
curl "http://localhost:8080/api/accounts/ACC-12345/interest?rate=0.05&days=30"
```

### Option 2: Using VS Code REST Client

1. **Install the REST Client extension** in VS Code
2. **Open** `demo/sample-requests.http`
3. **Click** "Send Request" above any request

### Option 3: Using Postman

1. **Import** the following base URL: `http://localhost:8080/api`
2. **Create requests** based on the API endpoints documented in README.md
3. **Use** the sample data from `demo/sample-data.json`

### Option 4: Using Your Browser

Visit these URLs in your browser:

- All transactions: http://localhost:8080/api/transactions
- Account balance: http://localhost:8080/api/accounts/ACC-12345/balance
- Account summary: http://localhost:8080/api/accounts/ACC-12345/summary
- Interest calculation: http://localhost:8080/api/accounts/ACC-12345/interest?rate=0.05&days=30

---

## 🛑 Stopping the Application

Press `Ctrl + C` in the terminal where the application is running.

---

## 🐛 Troubleshooting

### Problem: "Port 8080 already in use"

**Solution**:
1. Stop any application using port 8080
2. Or change the port in `src/main/resources/application.properties`:
   ```properties
   server.port=8081
   ```

### Problem: "Java version not supported"

**Error**: `Unsupported class file major version XX`

**Solution**: 
- Ensure Java 17 or higher is installed: `java -version`
- Update `JAVA_HOME` environment variable if needed

### Problem: "mvn command not found"

**Solution**:
1. Verify Maven installation: `mvn -version`
2. Add Maven to your PATH
3. Or use the Maven wrapper (if included): `./mvnw spring-boot:run`

### Problem: "Tests failing"

**Solution**:
1. Run tests with details: `mvn test -X`
2. Check test output for specific errors
3. Ensure no other instance is running on port 8081 (test port)

### Problem: "Dependencies not downloading"

**Solution**:
1. Check internet connection
2. Try: `mvn clean install -U` (force update)
3. Clear Maven cache: `rm -rf ~/.m2/repository`

---

## 📊 Sample Testing Workflow

1. **Start the application**:
   ```bash
   ./demo/run.sh
   ```

2. **Create some transactions** (in new terminal):
   ```bash
   # Deposit
   curl -X POST http://localhost:8080/api/transactions \
     -H "Content-Type: application/json" \
     -d '{"toAccount":"ACC-12345","amount":1000,"currency":"USD","type":"DEPOSIT"}'
   
   # Withdrawal
   curl -X POST http://localhost:8080/api/transactions \
     -H "Content-Type: application/json" \
     -d '{"fromAccount":"ACC-12345","amount":250,"currency":"USD","type":"WITHDRAWAL"}'
   ```

3. **Check balance**:
   ```bash
   curl http://localhost:8080/api/accounts/ACC-12345/balance
   ```
   Expected: `{"accountId":"ACC-12345","balance":750.00,"currency":"USD"}`

4. **Get summary**:
   ```bash
   curl http://localhost:8080/api/accounts/ACC-12345/summary
   ```

5. **Filter transactions**:
   ```bash
   curl "http://localhost:8080/api/transactions?accountId=ACC-12345&type=DEPOSIT"
   ```

---

## 🔧 Configuration

### Changing the Port

Edit `src/main/resources/application.properties`:

```properties
server.port=8080  # Change to desired port
```

### Changing Log Levels

Edit `src/main/resources/application.properties`:

```properties
logging.level.root=INFO
logging.level.com.banking.api=DEBUG  # Change to TRACE for more detail
```

---

## 📁 Project Structure Quick Reference

```
homework-1/
├── pom.xml                    # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/              # Application source code
│   │   └── resources/         # Configuration files
│   └── test/                  # Test files
├── demo/
│   ├── run.sh                 # Launch script (Unix/Mac)
│   ├── run.bat                # Launch script (Windows)
│   ├── test.sh                # Test script (Unix/Mac)
│   ├── test.bat               # Test script (Windows)
│   ├── sample-requests.http   # VS Code REST Client samples
│   └── sample-requests.sh     # curl command samples
├── README.md                  # Project documentation
└── HOWTORUN.md               # This file
```

---

## 💡 Tips for Success

1. **Always check** if the application is running before testing
2. **Use valid account formats**: ACC-XXXXX (5 alphanumeric characters)
3. **Use supported currencies**: USD, EUR, GBP, JPY, CHF, CAD, AUD, CNY, INR, MXN
4. **Keep amounts positive** with max 2 decimal places
5. **Check logs** if something doesn't work as expected
6. **Stop and restart** the application to reset data (in-memory storage)

---

## 🎯 Quick Command Reference

| Task | Command |
|------|---------|
| Run application | `./demo/run.sh` or `demo\run.bat` |
| Run tests | `./demo/test.sh` or `demo\test.bat` |
| Build only | `mvn clean install` |
| Run without build | `mvn spring-boot:run` |
| Run specific test | `mvn test -Dtest=TransactionServiceTest` |
| Check API status | `curl http://localhost:8080/api/transactions` |

---

## ❓ Need Help?

If you encounter issues:

1. Check the [Troubleshooting](#-troubleshooting) section
2. Review the console output for error messages
3. Ensure all prerequisites are installed correctly
4. Check that ports 8080 (app) and 8081 (tests) are available

---

<div align="center">

### ✅ You're all set! Happy testing! 🚀

</div>