import { validateTransactionInput, RawTransactionInput } from './transaction.validator';
import { ValidationException } from '../errors';

describe('Transaction Validator', () => {
  describe('validateTransactionInput', () => {
    describe('Valid inputs', () => {
      it('should validate a valid deposit', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        };

        const result = validateTransactionInput(input);

        expect(result.toAccount).toBe('ACC-00001');
        expect(result.amount).toBe(100);
        expect(result.currency).toBe('USD');
        expect(result.type).toBe('deposit');
      });

      it('should validate a valid withdrawal', () => {
        const input: RawTransactionInput = {
          fromAccount: 'ACC-00001',
          amount: 50,
          currency: 'EUR',
          type: 'withdrawal'
        };

        const result = validateTransactionInput(input);

        expect(result.fromAccount).toBe('ACC-00001');
        expect(result.type).toBe('withdrawal');
      });

      it('should validate a valid transfer', () => {
        const input: RawTransactionInput = {
          fromAccount: 'ACC-00001',
          toAccount: 'ACC-00002',
          amount: 200,
          currency: 'USD',
          type: 'transfer'
        };

        const result = validateTransactionInput(input);

        expect(result.fromAccount).toBe('ACC-00001');
        expect(result.toAccount).toBe('ACC-00002');
        expect(result.type).toBe('transfer');
      });

      it('should include optional description', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          amount: 100,
          currency: 'USD',
          type: 'deposit',
          description: 'Test deposit'
        };

        const result = validateTransactionInput(input);

        expect(result.description).toBe('Test deposit');
      });
    });

    describe('Invalid inputs', () => {
      it('should throw ValidationException for missing amount', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          currency: 'USD',
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for zero amount', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          amount: 0,
          currency: 'USD',
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for negative amount', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          amount: -100,
          currency: 'USD',
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for non-numeric amount', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          amount: 'hundred',
          currency: 'USD',
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for missing currency', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          amount: 100,
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for invalid type', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          amount: 100,
          currency: 'USD',
          type: 'invalid'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for deposit without toAccount', () => {
        const input: RawTransactionInput = {
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for withdrawal without fromAccount', () => {
        const input: RawTransactionInput = {
          amount: 100,
          currency: 'USD',
          type: 'withdrawal'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for transfer without fromAccount', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00002',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for transfer without toAccount', () => {
        const input: RawTransactionInput = {
          fromAccount: 'ACC-00001',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should include all validation errors in exception', () => {
        const input: RawTransactionInput = {};

        try {
          validateTransactionInput(input);
          fail('Should have thrown');
        } catch (error) {
          expect(error).toBeInstanceOf(ValidationException);
          const validationError = error as ValidationException;
          expect(validationError.errors.length).toBeGreaterThan(1);
        }
      });

      it('should throw ValidationException for invalid account format', () => {
        const input: RawTransactionInput = {
          toAccount: 'INVALID',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        };

        try {
          validateTransactionInput(input);
          fail('Should have thrown');
        } catch (error) {
          expect(error).toBeInstanceOf(ValidationException);
          const validationError = error as ValidationException;
          expect(validationError.errors).toContainEqual({
            field: 'toAccount',
            message: 'Account must match format ACC-XXXXX (5 alphanumeric characters)'
          });
        }
      });

      it('should throw ValidationException for invalid currency code', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          amount: 100,
          currency: 'INVALID',
          type: 'deposit'
        };

        try {
          validateTransactionInput(input);
          fail('Should have thrown');
        } catch (error) {
          expect(error).toBeInstanceOf(ValidationException);
          const validationError = error as ValidationException;
          expect(validationError.errors[0].field).toBe('currency');
          expect(validationError.errors[0].message).toContain('Invalid currency code');
        }
      });

      it('should throw ValidationException for amount with more than 2 decimal places', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          amount: 100.123,
          currency: 'USD',
          type: 'deposit'
        };

        try {
          validateTransactionInput(input);
          fail('Should have thrown');
        } catch (error) {
          expect(error).toBeInstanceOf(ValidationException);
          const validationError = error as ValidationException;
          expect(validationError.errors).toContainEqual({
            field: 'amount',
            message: 'Amount must have at most 2 decimal places'
          });
        }
      });

      it('should accept amount with exactly 2 decimal places', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC-00001',
          amount: 100.99,
          currency: 'USD',
          type: 'deposit'
        };

        const result = validateTransactionInput(input);
        expect(result.amount).toBe(100.99);
      });
    });
  });
});
