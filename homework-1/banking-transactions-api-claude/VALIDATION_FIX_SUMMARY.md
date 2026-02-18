# Validation Fix Summary

## Issue
When submitting a transaction without `fromAccount` and `toAccount` fields, the validation error response only showed the error for one field instead of both:

**Previous Response:**
```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "toAccount",
      "message": "To account is required for deposit transactions"
    }
  ]
}
```

**Expected Response:**
```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "fromAccount",
      "message": "From account is required"
    },
    {
      "field": "toAccount",
      "message": "To account is required"
    }
  ]
}
```

## Root Cause
The custom `@ValidTransactionAccounts` validator was checking fields based on transaction type and only reporting the first missing field it encountered. This prevented the standard `@NotBlank` validators from running on both fields.

## Solution
1. **Added `@NotBlank` annotations** to both `fromAccount` and `toAccount` fields in `CreateTransactionRequest`
2. **Removed the `@ValidTransactionAccounts` annotation** from the class level since it's no longer needed
3. **Simplified `TransactionAccountsValidator`** to always return true (can be used for future business rules)
4. **Updated all sample requests** to include both account fields

## Changes Made

### 1. CreateTransactionRequest.java
```java
public class CreateTransactionRequest {
    
    @NotBlank(message = "From account is required")
    @ValidAccountNumber
    private String fromAccount;

    @NotBlank(message = "To account is required")
    @ValidAccountNumber
    private String toAccount;
    
    // ...existing code...
}
```

### 2. TransactionAccountsValidator.java
```java
@Override
public boolean isValid(CreateTransactionRequest request, ConstraintValidatorContext context) {
    // Both fromAccount and toAccount are now required via @NotBlank validation
    // This validator can be used for additional business rules if needed
    return true;
}
```

### 3. sample-requests.http
Updated all examples to include both `fromAccount` and `toAccount`:
- Deposits now use `"fromAccount": "ACC-EXTERNAL"`
- Withdrawals now use `"toAccount": "ACC-EXTERNAL"`
- All validation examples updated

### 4. README.md
Updated documentation to reflect that both fields are always required for all transaction types.

## Verification

### Test Results
✅ All existing tests pass  
✅ New validation test created (`ValidationTest.java`)  
✅ Confirms both fields are reported when missing

### New Test Coverage
- `createTransaction_withBothAccountsMissing_returnsBothErrors()` - Verifies both errors are reported
- `createTransaction_withOnlyFromAccountMissing_returnsFromAccountError()` - Verifies fromAccount error
- `createTransaction_withOnlyToAccountMissing_returnsToAccountError()` - Verifies toAccount error

## Impact
- **Breaking Change**: Both `fromAccount` and `toAccount` are now mandatory for ALL transaction types (deposit, withdrawal, transfer)
- **Improved UX**: Users now see all validation errors at once instead of one at a time
- **Consistent API**: Same validation rules apply regardless of transaction type

