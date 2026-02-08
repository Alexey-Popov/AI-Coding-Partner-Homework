# Demo Scripts

This folder contains scripts and sample data to help you run and test the Banking Transactions API.

## 📁 Files

| File | Description |
|------|-------------|
| `run.sh` | Bash script to start the server on Linux/Mac |
| `run.bat` | Batch script to start the server on Windows |
| `sample-requests.http` | VS Code REST Client file with all API endpoints |
| `sample-requests.sh` | Interactive bash script to test all endpoints with curl |
| `sample-data.json` | Sample transaction and account data for reference |

## 🚀 Quick Start

### Starting the Server

**On Linux/Mac:**
```bash
chmod +x run.sh
./run.sh
```

**On Windows:**
```cmd
run.bat
```

## 🧪 Testing the API

### Option 1: Using VS Code REST Client

1. Install the [REST Client extension](https://marketplace.visualstudio.com/items?itemName=humao.rest-client) in VS Code
2. Open `sample-requests.http`
3. Click "Send Request" above any request to execute it
4. View responses inline

### Option 2: Using the Interactive Test Script

**On Linux/Mac:**
```bash
chmod +x sample-requests.sh
./sample-requests.sh
```

This script will:
- Test all API endpoints sequentially
- Display formatted JSON responses (requires `jq`)
- Pause between requests for review
- Show validation error examples

**Requirements:**
- `curl` (for making requests)
- `jq` (optional, for pretty-printing JSON)

### Option 3: Manual curl Commands

Copy individual commands from `sample-requests.http` or `sample-requests.sh` and run them directly in your terminal.

## 📊 Sample Data

The `sample-data.json` file contains example transactions and accounts that match the API's data structure. Use this as a reference for:
- Understanding the data model
- Seeding initial data (if implementing)
- Testing your implementation

## 💡 Tips

- Make sure the server is running before executing test scripts
- The default port is `3000` - modify the BASE_URL in scripts if different
- All test scripts assume the API is running at `http://localhost:3000`
- For the interactive script, install `jq` for better JSON formatting:
  ```bash
  # On Ubuntu/Debian
  sudo apt-get install jq

  # On macOS
  brew install jq
  ```
