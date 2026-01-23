# Demo Scripts - Usage Guide

This folder contains scripts to test and demonstrate the Banking Transaction API.

---

## 📁 Files Overview

### ✅ `populate-test-data.bat` (NEW - Windows)
**Purpose**: Automatically creates 10 diverse test transactions after server restart

**Usage**:
```bash
.\populate-test-data.bat
```

**What it creates**:
- 4 Deposits (USD, EUR, GBP, JPY, CAD across different accounts)
- 2 Withdrawals (USD, GBP)
- 3 Transfers (USD, EUR)
- Multiple accounts: ACC-12345, ACC-67890, ACC-ABC12, ACC-XYZ99

**When to use**: Run this every time you restart the server to quickly populate the database with realistic test data.

---

### ✅ `test-api.bat` (Updated - Windows)
**Purpose**: Basic API testing script with sample requests

**Usage**:
```bash
.\test-api.bat
```

**Tests**:
- Health check
- Create deposit (corrected format)
- Create withdrawal (corrected format)
- List all transactions
- Get account balance
- Get transaction summary

**Fixed Issues**: 
- ✅ Deposits now correctly exclude `fromAccount`
- ✅ Withdrawals now correctly exclude `toAccount`

---

### ✅ `sample-requests.sh` (Updated - Linux/Mac)
**Purpose**: Shell script for testing API with cURL

**Usage**:
```bash
chmod +x sample-requests.sh
./sample-requests.sh
```

**Fixed Issues**: 
- ✅ Deposits now correctly exclude `fromAccount`
- ✅ Withdrawals now correctly exclude `toAccount`

---

### ✅ `sample-requests.http` (Updated - VS Code REST Client)
**Purpose**: Interactive API testing in VS Code using REST Client extension

**Usage**:
1. Install [REST Client extension](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
2. Open `sample-requests.http` in VS Code
3. Click "Send Request" above any request

**What's included**:
- ✅ All CRUD operations
- ✅ Filtering examples
- ✅ Valid transaction examples (deposit, withdrawal, transfer)
- ✅ Invalid request examples (for testing validation)

**Fixed Issues**: 
- ✅ Deposits now correctly exclude `fromAccount`
- ✅ Withdrawals now correctly exclude `toAccount`
- ✅ Added new validation test cases:
  - Deposit with fromAccount (should fail)
  - Withdrawal with toAccount (should fail)
  - Transfer missing required account (should fail)

---

## 🚀 Quick Start Workflow

### After Server Restart:
```bash
# 1. Start the server (in one terminal)
npm start

# 2. Populate test data (in another terminal)
cd demo
.\populate-test-data.bat

# 3. View results at http://localhost:3000
```

### For Manual Testing:
- **Windows**: Use `test-api.bat` or `sample-requests.http`
- **Linux/Mac**: Use `sample-requests.sh` or `sample-requests.http`

---

## 🔍 Review Summary

### ❌ **Issues Found in Original Demo Files**:

1. **Incorrect Transaction Format** (Critical)
   - Deposits had `fromAccount` (should only have `toAccount`)
   - Withdrawals had `toAccount` (should only have `fromAccount`)
   - This violated the new validation rules implemented in the backend

2. **Missing Test Data Population**
   - No easy way to restore data after server restart
   - Manual transaction creation was tedious

### ✅ **What Was Fixed**:

1. **All demo scripts updated** to match the new validation logic:
   - `sample-requests.http` ✅
   - `test-api.bat` ✅
   - `sample-requests.sh` ✅

2. **New test cases added** to `sample-requests.http`:
   - Invalid deposit with fromAccount
   - Invalid withdrawal with toAccount
   - Invalid transfer missing accounts

3. **New script created**: `populate-test-data.bat`
   - Creates 10 realistic test transactions
   - Uses multiple currencies (USD, EUR, GBP, JPY, CAD)
   - Uses multiple accounts
   - Runs in seconds

---

## 💡 Best Practices

### Transaction Type Rules:
- **Deposit**: Only `toAccount` (money coming in)
- **Withdrawal**: Only `fromAccount` (money going out)
- **Transfer**: Both `fromAccount` and `toAccount` (money moving between accounts)

### Account Format:
- Must follow pattern: `ACC-XXXXX` (where X is alphanumeric)
- Examples: `ACC-12345`, `ACC-ABC12`, `ACC-XYZ99`

### Currency Codes:
- Must be valid ISO 4217 codes
- Supported: USD, EUR, GBP, JPY, AUD, CAD, CHF, CNY, SEK, etc.

### Amount Format:
- Must be positive
- Maximum 2 decimal places
- Example: 100.50 ✅, 100.505 ❌, -50 ❌

---

## 🎯 Recommendations

1. **Always run `populate-test-data.bat` after server restart** to have realistic data to work with
2. **Use `sample-requests.http` in VS Code** for interactive testing (easier than curl)
3. **Test both valid and invalid scenarios** to ensure validation works correctly
4. **Check the UI at http://localhost:3000** to see transactions visually

---

## 📝 Notes

- All scripts assume the server is running on `http://localhost:3000`
- Test data is stored in-memory and will be lost on server restart
- The populate script can be run multiple times (adds 10 more transactions each time)
