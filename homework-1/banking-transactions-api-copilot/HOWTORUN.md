# 🚀 How to Run the Banking Transactions API

This guide provides step-by-step instructions to build and run the Banking Transactions API on your local machine.

---

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

### Required Software

| Software | Version | Check Command |
|----------|---------|---------------|
| **Java JDK** | 21 or higher | `java -version` |
| **Gradle** | 8.5 or higher | `gradle -version` |

### Installation Instructions

#### macOS
```bash
# Install Java 21 using Homebrew
brew install openjdk@21

# Add Java to PATH (add to ~/.zshrc or ~/.bash_profile)
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
```

#### Linux
```bash
# Install Java 21 using apt (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-21-jdk

# Verify installation
java -version
```

#### Windows
1. Download Java 21 JDK from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/)
2. Install and add to PATH
3. Verify with `java -version` in Command Prompt

**Note**: Gradle wrapper is included in the project, so you don't need to install Gradle separately.

---

## 🔨 Building the Application

### Step 1: Navigate to Project Directory
```bash
cd homework-1
```

### Step 2: Make Gradle Wrapper Executable (macOS/Linux)
```bash
chmod +x gradlew
```

### Step 3: Build the Project
```bash
# Build without running tests
./gradlew build -x test --quiet

# OR build with tests
./gradlew build
```

**Expected Output**: JAR file will be created at `build/libs/banking-transactions-api-1.0.0.jar`

### Verify Build
```bash
ls -la build/libs/
# Should show: banking-transactions-api-1.0.0.jar
```

---

## ▶️ Running the Application

### Option 1: Run with Java JAR (Recommended)

```bash
# Run the application
java -jar build/libs/banking-transactions-api-1.0.0.jar
```

### Option 2: Run with Gradle (Development Mode)

```bash
# Run directly with Gradle
./gradlew bootRun
```

### Option 3: Run in Background (macOS/Linux)

```bash
# Run in background
java -jar build/libs/banking-transactions-api-1.0.0.jar > /dev/null 2>&1 &

# Save the process ID
echo $! > app.pid
```

### Application Startup

When the application starts successfully, you should see:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.1)

...
Tomcat started on port 3000 (http) with context path ''
Started BankingTransactionsApplication in X.XXX seconds
```

**Server URL**: `http://localhost:3000`

---

## 🧪 Testing the API

### Quick Health Check

```bash
# Test if the server is running
curl http://localhost:3000/transactions
```

**Expected Response**: `[]` (empty array - no transactions yet)

### Create a Test Transaction

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }'
```

**Expected Response**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2026-01-24T12:00:00",
  "status": "completed"
}
```

### Use Sample Requests File

The project includes a `demo/sample-requests.http` file with pre-configured API requests. You can use:

- **VS Code**: Install [REST Client extension](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
- **IntelliJ IDEA**: Built-in HTTP client support

---

## 🛑 Stopping the Application

### If Running in Foreground
Press `Ctrl + C` in the terminal

### If Running in Background

#### Using Process ID
```bash
# If you saved the PID
kill $(cat app.pid)
rm app.pid
```

#### Find and Kill by Port
```bash
# macOS/Linux
lsof -ti:3000 | xargs kill -9

# OR find process manually
lsof -i:3000
kill -9 <PID>
```

#### Find and Kill by Name
```bash
# macOS/Linux
pkill -f "banking-transactions-api"

# OR
ps aux | grep "banking-transactions-api"
kill -9 <PID>
```

---

## 🔧 Troubleshooting

### Port 3000 Already in Use

**Error**: `Port 3000 is already in use`

**Solution**:
```bash
# Kill the process using port 3000
lsof -ti:3000 | xargs kill -9

# OR change the port in src/main/resources/application.properties
server.port=8080
```

### Java Version Mismatch

**Error**: `Unsupported class file major version`

**Solution**:
```bash
# Check Java version
java -version

# Should be Java 21 or higher
# If not, install Java 21 and update JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Build Fails

**Error**: `Could not resolve dependencies`

**Solution**:
```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies
```

### Gradle Wrapper Not Executable (macOS/Linux)

**Error**: `Permission denied: ./gradlew`

**Solution**:
```bash
chmod +x gradlew
```

---

## 📝 Additional Resources

### View Application Logs

```bash
# If running in background, check logs
tail -f nohup.out
```

### Rebuild After Code Changes

```bash
# Stop the application first
lsof -ti:3000 | xargs kill -9

# Clean and rebuild
./gradlew clean build -x test --quiet

# Restart
java -jar build/libs/banking-transactions-api-1.0.0.jar
```

### API Documentation

All available endpoints are documented in:
- `README.md` - Feature overview
- `demo/sample-requests.http` - Sample requests for testing

---

## 🎯 Quick Start Summary

```bash
# 1. Navigate to project directory
cd homework-1

# 2. Build the project
./gradlew build -x test --quiet

# 3. Run the application
java -jar build/libs/banking-transactions-api-1.0.0.jar

# 4. Test in another terminal
curl http://localhost:3000/transactions

# 5. Stop the application
# Press Ctrl+C or kill the process
```

---

## ⚡ Development Workflow

For active development with auto-reload:

```bash
# Terminal 1: Run application in development mode
./gradlew bootRun

# Terminal 2: Make code changes and rebuild
./gradlew build -x test

# Then restart the application in Terminal 1 (Ctrl+C and re-run)
```

---

## 🆘 Need Help?

If you encounter issues:

1. Check Prerequisites are correctly installed
2. Verify port 3000 is not in use
3. Review the Troubleshooting section
4. Check application logs for error details

For detailed API usage, refer to `README.md` and `demo/sample-requests.http`.
