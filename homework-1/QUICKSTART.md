# 🚀 Quick Start Guide - Banking Transactions API Copilot

## ⚡ TL;DR - Get Started in 30 Seconds

```bash
# 1. Start the API (Terminal 1)
./gradlew :banking-transactions-api-copilot:bootRun

# 2. Load sample data (Terminal 2 - wait for API to start first)
./demo/load-sample-data.sh

# 3. Test it works
curl http://localhost:3000/transactions
```

---

## 📦 What You Have

✅ **New Module:** `banking-transactions-api-copilot` with all app files  
✅ **Sample Data:** 4 accounts + 10 transactions ready to load  
✅ **Scripts:** Automated loading and running scripts  
✅ **Documentation:** Complete guides and verification reports  

---

## 🎯 Common Commands

### Build & Run
```bash
# Build the module
./gradlew :banking-transactions-api-copilot:build

# Run the application
./gradlew :banking-transactions-api-copilot:bootRun

# Run tests
./gradlew :banking-transactions-api-copilot:test
```

### Sample Data
```bash
# Load all sample data automatically
./demo/load-sample-data.sh

# View what was loaded
curl http://localhost:3000/transactions | python3 -m json.tool
curl http://localhost:3000/accounts/ACC-12345/balance | python3 -m json.tool
```

### Manual Testing
```bash
# Create a transaction
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{"fromAccount":"ACC-12345","toAccount":"ACC-67890","amount":100,"currency":"USD","type":"transfer"}'

# List all transactions
curl http://localhost:3000/transactions

# Check account balance
curl http://localhost:3000/accounts/ACC-12345/balance
```

---

## 📚 Documentation

| File | What It Contains |
|------|------------------|
| `MIGRATION_SUMMARY.md` | Complete migration overview |
| `HOWTORUN.md` | Detailed setup instructions |
| `demo/SAMPLE_DATA_USAGE.md` | How to use sample data |
| `demo/QUICK_REFERENCE.md` | Quick command reference |
| `banking-transactions-api-copilot/VERIFICATION_REPORT.md` | Test results |

---

## ✅ Verification

**Everything is working!** ✨

- ✅ Module builds successfully
- ✅ All tests pass
- ✅ Application starts on port 3000
- ✅ API endpoints respond correctly
- ✅ Sample data ready to use

---

## 🆘 Troubleshooting

### Port 3000 already in use
```bash
# Kill existing process
lsof -ti:3000 | xargs kill -9
```

### Need to reset data
```bash
# Just restart the application (in-memory database)
# Press Ctrl+C to stop, then start again
```

### jq not found (for pretty JSON)
```bash
# macOS
brew install jq

# Or use python instead
curl http://localhost:3000/transactions | python3 -m json.tool
```

---

## 🎉 You're Ready!

The banking-transactions-api-copilot module is fully set up and verified to work. Happy coding! 🚀

