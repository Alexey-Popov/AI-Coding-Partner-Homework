# 🚀 How to Run the Banking Transactions API

This guide provides step-by-step instructions for setting up and running the Banking Transactions REST API on your local machine.

---

## 📋 Table of Contents

1. [Prerequisites](#-prerequisites)
2. [Installation Steps](#-installation-steps)
3. [Starting the Application](#-starting-the-application)
4. [Testing the API](#-testing-the-api)
5. [Sample Test Flow](#-sample-test-flow)
6. [Stopping the Application](#-stopping-the-application)
7. [Troubleshooting](#-troubleshooting)

---

## ✅ Prerequisites

Before running the application, ensure you have the following installed on your system:

### Required Software

| Software | Minimum Version | Purpose | Download Link |
|----------|----------------|---------|---------------|
| **Java JDK** | **21+** (Java 21, 22, 23, 24, or 25) | Runtime environment | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/) |
| **Maven** | 3.6+ | Build tool | [Apache Maven](https://maven.apache.org/download.cgi) |
| **Git** | 2.0+ | Version control | [Git Downloads](https://git-scm.com/downloads) |
| **curl** | Any version | API testing (optional) | Pre-installed on macOS/Linux, [Windows](https://curl.se/windows/) |

### Java Version Note

**This project requires Java 21 or higher.** The project compiles to Java 21 bytecode using Spring Boot 3.4.2 and Lombok 1.18.42, and is compatible with Java 21, 22, 23, 24, and 25.

### Supported Operating Systems

- ✅ macOS (10.14+)
- ✅ Linux (Ubuntu 18.04+, Fedora, CentOS)
- ✅ Windows 10/11 (with Git Bash or PowerShell)

### Verify Prerequisites

Check that all required software is installed and accessible:

```bash
# Check Java version (must be 21 or higher)
java -version

# Check Maven version
mvn -version

# Check Git version
git --version

# Check curl (optional, for testing)
curl --version
```

**Expected Output for Java:**
```
openjdk version "21.0.x" or higher (21, 22, 23, 24, or 25)
OpenJDK Runtime Environment (build ...)
```

**Expected Output for Maven:**
```
Apache Maven 3.x.x
Maven home: /path/to/maven
Java version: 21.0.x (or higher)
```

> ⚠️ **Important:** If any command fails with "command not found", please install the missing software before proceeding.

---

## 📥 Installation Steps

Follow these steps to set up the project on your local machine:

### Step 1: Clone the Repository

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/AI-Coding-Partner-Homework.git

# Navigate to the homework-1 directory
cd AI-Coding-Partner-Homework/homework-1
```

### Step 2: Verify Project Structure

Ensure the project structure looks like this:

```
homework-1/
├── pom.xml                 # Maven configuration
├── README.md               # Project documentation
├── HOWTORUN.md            # This file
├── src/
│   └── main/
│       ├── java/          # Java source code
│       └── resources/     # Configuration files
└── demo/                  # Test scripts and samples
```

### Step 3: Review Application Configuration

The application configuration is located in `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=3000
server.servlet.context-path=/api/v1

# Logging
logging.level.root=INFO
logging.level.com.banking.api=DEBUG

# CORS Configuration
spring.web.cors.allowed-origins=*
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
```

> 💡 **Tip:** The API runs on **port 3000** by default. If this port is already in use, you can change it by editing `server.port` in `application.properties`.

### Step 4: Build the Project

Build the project and download all dependencies:

```bash
# Clean any previous builds and compile
mvn clean install
```

**Expected Output:**
```
[INFO] Scanning for projects...
[INFO] Building banking-api 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:3.2.0:clean (default-clean) @ banking-api ---
[INFO] --- maven-resources-plugin:3.3.0:resources (default-resources) @ banking-api ---
[INFO] --- maven-compiler-plugin:3.11.0:compile (default-compile) @ banking-api ---
[INFO] --- maven-jar-plugin:3.3.0:jar (default-jar) @ banking-api ---
[INFO] BUILD SUCCESS
```

> ⚠️ **Note:** The first build may take 2-5 minutes as Maven downloads all dependencies. Subsequent builds will be faster.

---

## ▶️ Starting the Application

You can start the application using any of the following methods:

### Method 1: Using the Convenience Script (Recommended)

```bash
# Make the script executable (first time only)
chmod +x demo/run.sh

# Run the script
./demo/run.sh
```

### Method 2: Manual Build and Run (Unix/Mac/Linux)

```bash
# Build and prepare dependencies
mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/dependency -DincludeScope=runtime

# Run the application
java -cp "target/classes:target/dependency/*" com.banking.api.BankingApiApplication
```

### Method 3: Using the Convenience Script (Windows)

```cmd
# Run from Command Prompt or PowerShell
demo\run.bat
```

### Method 4: Running the JAR Directly

```bash
# Build the JAR file
mvn clean package -DskipTests

# Run the JAR
java -jar target/banking-transactions-api-1.0.0.jar
```

---

## ✅ Expected Startup Output

When the application starts successfully, you should see:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.4.2)

2026-01-26 10:30:00.123  INFO 12345 --- [           main] c.b.api.BankingApiApplication            : Starting BankingApiApplication
2026-01-26 10:30:01.456  INFO 12345 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 3000 (http)
2026-01-26 10:30:02.789  INFO 12345 --- [           main] c.b.api.BankingApiApplication            : Started BankingApiApplication in 2.666 seconds
```

**Key Indicators of Successful Startup:**

✅ **"Started BankingApiApplication"** message appears  
✅ **"Tomcat initialized with port 3000"** confirms the server port  
✅ No error messages in red text  
✅ Application continues running (doesn't exit)

---

## 🧪 Testing the API

### Quick Health Check

Verify the application is running:

```bash
curl http://localhost:3000/api/v1/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

> ✅ If you see `{"status":"UP"}`, the API is ready to use!

---

## 🎯 Sample Test Flow

This section demonstrates a complete workflow from creating transactions to exporting data.

### Step 1: Create a Deposit Transaction

```bash
curl -X POST http://localhost:3000/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 1000.00,
    "currency": "USD",
    "type": "deposit"
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "toAccount": "ACC-12345",
  "amount": 1000.00,
  "currency": "USD",
  "type": "DEPOSIT",
  "timestamp": "2026-01-26T10:30:00",
  "status": "COMPLETED"
}
```

### Step 2: Create a Transfer Transaction

```bash
curl -X POST http://localhost:3000/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 250.50,
    "currency": "USD",
    "type": "transfer"
  }'
```

### Step 3: Create a Withdrawal Transaction

```bash
curl -X POST http://localhost:3000/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "amount": 100.00,
    "currency": "USD",
    "type": "withdrawal"
  }'
```

### Step 4: View All Transactions

```bash
curl http://localhost:3000/api/v1/transactions
```

**Expected Response:** Array of all transactions created.

### Step 5: Check Account Balance

```bash
curl http://localhost:3000/api/v1/accounts/ACC-12345/balance
```

**Expected Response:**
```json
{
  "accountId": "ACC-12345",
  "balance": 649.50,
  "currency": "USD",
  "calculatedAt": "2026-01-26T10:35:00"
}
```

**Calculation:** $1000.00 (deposit) - $250.50 (transfer out) - $100.00 (withdrawal) = **$649.50**

### Step 6: Filter Transactions by Account

```bash
curl "http://localhost:3000/api/v1/transactions?accountId=ACC-12345"
```

### Step 7: Get Transaction Summary

```bash
curl http://localhost:3000/api/v1/accounts/ACC-12345/summary
```

**Expected Response:**
```json
{
  "accountId": "ACC-12345",
  "totalDeposits": 1000.00,
  "totalWithdrawals": 350.50,
  "numberOfTransactions": 3,
  "mostRecentTransactionDate": "2026-01-26T10:35:00",
  "currentBalance": 649.50,
  "currency": "USD"
}
```

### Step 8: Calculate Interest

Calculate simple interest for 30 days at 5% annual rate:

```bash
curl "http://localhost:3000/api/v1/accounts/ACC-12345/interest?rate=0.05&days=30"
```

**Expected Response:**
```json
{
  "accountId": "ACC-12345",
  "currentBalance": 649.50,
  "interestRate": 0.05,
  "days": 30,
  "interestAmount": 2.67,
  "projectedBalance": 652.17,
  "formula": "Principal × Rate × (Days/365)",
  "currency": "USD"
}
```

### Step 9: Export Transactions to CSV

```bash
curl "http://localhost:3000/api/v1/transactions/export?format=csv" \
  --output transactions.csv
```

**Expected Result:** File `transactions.csv` downloaded with all transactions.

---

## 🔬 Using Demo Test Scripts

The `demo/` folder contains comprehensive test scripts for automated testing.

### Validation Tests

Tests all validation rules (amounts, account numbers, currencies, business rules):

```bash
cd demo
chmod +x validation-tests.sh
./validation-tests.sh
```

**Features:**
- ✅ 27 validation test cases
- ✅ Color-coded output (green = pass, red = fail)
- ✅ Automatic pass/fail counting
- ✅ Tests invalid amounts, account formats, currencies, and business rules

**Expected Output:**
```
🧪 Running Banking API Validation Tests...
========================================

Testing invalid amounts...
✅ Test 1/27: Negative amount rejected
✅ Test 2/27: Zero amount rejected
✅ Test 3/27: Too many decimals rejected
...

========================================
✅ Validation Tests Complete: 27/27 passed
```

### Filtering & Features Tests

Tests filtering, summaries, interest, CSV export, and rate limiting:

```bash
cd demo
chmod +x filtering-tests.sh
./filtering-tests.sh
```

**Features:**
- ✅ 21+ filtering and feature tests
- ✅ Tests all 5 filter parameters
- ✅ Tests combined filters
- ✅ Tests additional features (summary, interest, CSV, rate limiting)
- ✅ Comprehensive test coverage

**Expected Output:**
```
🧪 Banking API - Filtering & Additional Features Tests
======================================================

📋 Phase 1: Setup Test Data
✅ Test 1/21: Created deposit to ACC-12345
✅ Test 2/21: Created withdrawal from ACC-12345
...

📋 Phase 6: Rate Limiting Tests
✅ Test 21/21: Rate limit enforced after 100 requests

======================================================
✅ All Tests Passed: 21/21
```

### Using Sample Requests (REST Client)

If you're using VS Code with the REST Client extension:

1. Open `demo/sample-requests.http`
2. Click "Send Request" above any request
3. View responses in the side panel

**Sample requests include:**
- Create transactions (all types)
- Get all transactions
- Filter transactions
- Check balance
- Get summary
- Calculate interest
- Export CSV

---

## 🛑 Stopping the Application

### Method 1: Using Keyboard Interrupt

Press `Ctrl + C` in the terminal where the application is running.

**Expected Output:**
```
^C
2026-01-26 10:45:00.123  INFO 12345 --- [ionShutdownHook] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat stopped
```

### Method 2: Finding and Killing the Process

If the application is running in the background:

**macOS/Linux:**
```bash
# Find the process ID
lsof -i :3000

# Kill the process (replace PID with actual process ID)
kill -9 <PID>
```

**Windows:**
```cmd
# Find the process using port 3000
netstat -ano | findstr :3000

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

### Method 3: Using Maven

If started with `mvn spring-boot:run`, press `Ctrl + C`.

---

## 🔧 Troubleshooting

### Issue 1: Port Already in Use

**Error Message:**
```
Web server failed to start. Port 3000 was already in use.
```

**Solutions:**

**Option A: Change the Port**

Edit `src/main/resources/application.properties`:
```properties
server.port=8080
```

Then restart the application.

**Option B: Kill the Process Using Port 3000**

**macOS/Linux:**
```bash
# Find what's using port 3000
lsof -i :3000

# Kill the process
kill -9 <PID>
```

**Windows:**
```cmd
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

---

### Issue 2: Java Version Mismatch

**Error Message:**
```
Unsupported major.minor version 65.0
```

**Cause:** You're using Java 17 or earlier, but the project requires Java 21+.

**Solution:**

1. Install Java 21 or higher (see [Prerequisites](#-prerequisites))
2. Verify installation:
   ```bash
   java -version
   ```
3. If multiple Java versions are installed, set `JAVA_HOME`:

**macOS/Linux:**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

**Windows:**
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-21
```

---

### Issue 3: Lombok Compilation Errors in IDE

**Error Message:**
```
cannot find symbol: method getType()
cannot find symbol: method builder()
```

**Cause:** IDE doesn't have Lombok annotation processing enabled.

**Solution:**

This project uses Lombok 1.18.42 which is compatible with Java 21-25. The issue is typically IDE configuration.

**IntelliJ IDEA:**
1. Install "Lombok" plugin from Settings > Plugins
2. Enable annotation processing: Settings > Build > Compiler > Annotation Processors
3. Check "Enable annotation processing"
4. Rebuild the project

**VS Code:**
1. Install "Language Support for Java" extension
2. The extension auto-detects Lombok from pom.xml
3. Reload window if needed

**Eclipse:**
1. Download `lombok.jar` from [projectlombok.org](https://projectlombok.org/)
2. Run: `java -jar lombok.jar`
3. Follow installer to add to Eclipse

---

### Issue 4: Maven Build Fails

**Error Message:**
```
[ERROR] Failed to execute goal ... compilation failure
```

**Solutions:**

**Clean and rebuild:**
```bash
mvn clean install -U
```

**Skip tests if test compilation fails:**
```bash
mvn clean install -DskipTests
```

**Verify Maven settings:**
```bash
mvn -version
```

---

### Issue 5: Connection Refused

**Error Message:**
```
curl: (7) Failed to connect to localhost port 3000: Connection refused
```

**Cause:** Application is not running.

**Solutions:**

1. **Check if application is running:**
   ```bash
   # macOS/Linux
   lsof -i :3000
   
   # Windows
   netstat -ano | findstr :3000
   ```

2. **Start the application** (see [Starting the Application](#%EF%B8%8F-starting-the-application))

3. **Check application logs** for errors during startup

---

### Issue 6: Tests Fail with "Command Not Found"

**Error Message:**
```bash
./validation-tests.sh: command not found
```

**Solutions:**

**Make scripts executable:**
```bash
chmod +x demo/validation-tests.sh
chmod +x demo/filtering-tests.sh
chmod +x demo/run.sh
```

**Ensure bash is available:**
```bash
# Check bash location
which bash

# On Windows, use Git Bash or WSL
```

---

### Issue 7: JSON Parse Error

**Error Message:**
```
curl: (3) URL using bad/illegal format or missing URL
```

**Cause:** Shell interpretation of special characters in JSON.

**Solution:**

**Use single quotes on Unix/Mac:**
```bash
curl -X POST http://localhost:3000/api/v1/transactions \
  -H 'Content-Type: application/json' \
  -d '{"toAccount":"ACC-12345","amount":100.00,"currency":"USD","type":"deposit"}'
```

**Use escape characters on Windows:**
```cmd
curl -X POST http://localhost:3000/api/v1/transactions ^
  -H "Content-Type: application/json" ^
  -d "{\"toAccount\":\"ACC-12345\",\"amount\":100.00,\"currency\":\"USD\",\"type\":\"deposit\"}"
```

**Or save JSON to file:**
```bash
curl -X POST http://localhost:3000/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d @request.json
```

---

### Issue 8: Rate Limit Exceeded

**Error Message:**
```json
{
  "error": "Rate limit exceeded. Maximum 100 requests per minute allowed.",
  "timestamp": "2026-01-26T10:30:00"
}
```

**Cause:** You've exceeded 100 requests per minute from your IP.

**Solution:**

**Wait for rate limit to reset:**
- Check `Retry-After` header for seconds to wait
- Or wait 60 seconds from first request

**Temporary workaround for testing:**
- Restart the application (clears rate limit counters)
- Or test from different IP address

---

## 📚 Additional Resources

- **Project Documentation:** [README.md](README.md)
- **Architecture Details:** [architecture.md](architecture.md)
- **Assignment Requirements:** [TASKS.md](TASKS.md)
- **Spring Boot Documentation:** [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- **REST API Best Practices:** [https://restfulapi.net/](https://restfulapi.net/)

---

## 💡 Quick Reference

### Common Commands

```bash
# Start application (recommended)
./demo/run.sh

# Or build and run manually
mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/dependency -DincludeScope=runtime
java -cp "target/classes:target/dependency/*" com.banking.api.BankingApiApplication

# Run tests
cd demo && ./validation-tests.sh && ./filtering-tests.sh

# Health check
curl http://localhost:3000/api/v1/actuator/health

# Create deposit
curl -X POST http://localhost:3000/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"toAccount":"ACC-12345","amount":100.00,"currency":"USD","type":"deposit"}'

# Get all transactions
curl http://localhost:3000/api/v1/transactions

# Check balance
curl http://localhost:3000/api/v1/accounts/ACC-12345/balance
```

### Default Configuration

| Setting | Value |
|---------|-------|
| **Server Port** | 3000 |
| **Base URL** | http://localhost:3000/api/v1 |
| **Rate Limit** | 100 requests/minute per IP |
| **Storage** | In-memory (ConcurrentHashMap) |
| **Health Check** | /api/v1/actuator/health |

---

## ✅ Success Checklist

Before submitting or deploying, verify:

- [ ] Application starts without errors
- [ ] Health check returns `{"status":"UP"}`
- [ ] Can create deposit transaction
- [ ] Can create withdrawal transaction
- [ ] Can create transfer transaction
- [ ] Can retrieve all transactions
- [ ] Can retrieve transaction by ID
- [ ] Can check account balance
- [ ] Filtering works (accountId, type, status, date range)
- [ ] Transaction summary endpoint works
- [ ] Interest calculation endpoint works
- [ ] CSV export works
- [ ] Rate limiting enforces 100 req/min
- [ ] Validation tests pass (27/27)
- [ ] Filtering tests pass (21/21)

---

## 🆘 Need Help?

If you encounter issues not covered in this guide:

1. **Check application logs** for detailed error messages
2. **Review [TASKS.md](TASKS.md)** for requirements
3. **Consult [architecture.md](architecture.md)** for system design
4. **Check GitHub Issues** for similar problems
5. **Contact the instructor** with:
   - Error message (full stacktrace)
   - Steps to reproduce
   - Your environment details (OS, Java version, Maven version)

---

<div align="center">

**Happy Testing!**

*Built with Java 21+ • Spring Boot 3.4.2 • Maven*

</div>