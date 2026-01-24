import { validateTransactionInput, RawTransactionInput } from './transaction.validator';
import { ValidationException } from '../errors';

describe('Transaction Validator', () => {
  describe('validateTransactionInput', () => {
    describe('Valid inputs', () => {
      it('should validate a valid deposit', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC001',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        };

        const result = validateTransactionInput(input);

        expect(result.toAccount).toBe('ACC001');
        expect(result.amount).toBe(100);
        expect(result.currency).toBe('USD');
        expect(result.type).toBe('deposit');
      });

      it('should validate a valid withdrawal', () => {
        const input: RawTransactionInput = {
          fromAccount: 'ACC001',
          amount: 50,
          currency: 'EUR',
          type: 'withdrawal'
        };

        const result = validateTransactionInput(input);

        expect(result.fromAccount).toBe('ACC001');
        expect(result.type).toBe('withdrawal');
      });

      it('should validate a valid transfer', () => {
        const input: RawTransactionInput = {
          fromAccount: 'ACC001',
          toAccount: 'ACC002',
          amount: 200,
          currency: 'USD',
          type: 'transfer'
        };

        const result = validateTransactionInput(input);

        expect(result.fromAccount).toBe('ACC001');
        expect(result.toAccount).toBe('ACC002');
        expect(result.type).toBe('transfer');
      });

      it('should include optional description', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC001',
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
          toAccount: 'ACC001',
          currency: 'USD',
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for zero amount', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC001',
          amount: 0,
          currency: 'USD',
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for negative amount', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC001',
          amount: -100,
          currency: 'USD',
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for non-numeric amount', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC001',
          amount: 'hundred',
          currency: 'USD',
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for missing currency', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC001',
          amount: 100,
          type: 'deposit'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for invalid type', () => {
        const input: RawTransactionInput = {
          toAccount: 'ACC001',
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
          toAccount: 'ACC002',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        };

        expect(() => validateTransactionInput(input)).toThrow(ValidationException);
      });

      it('should throw ValidationException for transfer without toAccount', () => {
        const input: RawTransactionInput = {
          fromAccount: 'ACC001',
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
    });
  });
});
