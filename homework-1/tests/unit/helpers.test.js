const { describe, it } = require('node:test');
const assert = require('node:assert');
const { toCSV, parseDate, formatError, formatSuccess } = require('../../src/utils/helpers');

describe('Helper Functions', () => {
  describe('toCSV', () => {
    it('should return empty string for empty array', () => {
      const result = toCSV([]);
      assert.strictEqual(result, '');
    });

    it('should return empty string for null input', () => {
      const result = toCSV(null);
      assert.strictEqual(result, '');
    });

    it('should return empty string for undefined input', () => {
      const result = toCSV(undefined);
      assert.strictEqual(result, '');
    });

    it('should convert simple object array to CSV', () => {
      const data = [
        { id: 1, name: 'John' },
        { id: 2, name: 'Jane' }
      ];
      const result = toCSV(data);
      assert.strictEqual(result, 'id,name\n1,John\n2,Jane');
    });

    it('should handle values with commas by quoting them', () => {
      const data = [
        { id: 1, description: 'Hello, World' }
      ];
      const result = toCSV(data);
      assert.strictEqual(result, 'id,description\n1,"Hello, World"');
    });

    it('should handle values with quotes by escaping them', () => {
      const data = [
        { id: 1, message: 'He said "Hello"' }
      ];
      const result = toCSV(data);
      assert.strictEqual(result, 'id,message\n1,"He said ""Hello"""');
    });

    it('should handle values with newlines', () => {
      const data = [
        { id: 1, text: 'Line 1\nLine 2' }
      ];
      const result = toCSV(data);
      assert.strictEqual(result, 'id,text\n1,"Line 1\nLine 2"');
    });

    it('should handle null and undefined values', () => {
      const data = [
        { id: 1, value: null },
        { id: 2, value: undefined }
      ];
      const result = toCSV(data);
      // null and undefined are converted to empty values in CSV
      assert.strictEqual(result, 'id,value\n1,\n2,');
    });

    it('should handle numeric values', () => {
      const data = [
        { amount: 100.50, count: 5 }
      ];
      const result = toCSV(data);
      assert.strictEqual(result, 'amount,count\n100.5,5');
    });

    it('should handle boolean values', () => {
      const data = [
        { active: true, deleted: false }
      ];
      const result = toCSV(data);
      assert.strictEqual(result, 'active,deleted\ntrue,false');
    });

    it('should handle single row', () => {
      const data = [{ id: 1 }];
      const result = toCSV(data);
      assert.strictEqual(result, 'id\n1');
    });

    it('should handle multiple columns', () => {
      const data = [
        { a: 1, b: 2, c: 3, d: 4, e: 5 }
      ];
      const result = toCSV(data);
      assert.strictEqual(result, 'a,b,c,d,e\n1,2,3,4,5');
    });
  });

  describe('parseDate', () => {
    it('should return null for null input', () => {
      const result = parseDate(null);
      assert.strictEqual(result, null);
    });

    it('should return null for undefined input', () => {
      const result = parseDate(undefined);
      assert.strictEqual(result, null);
    });

    it('should return null for empty string', () => {
      const result = parseDate('');
      assert.strictEqual(result, null);
    });

    it('should return null for invalid date string', () => {
      const result = parseDate('not-a-date');
      assert.strictEqual(result, null);
    });

    it('should parse valid ISO date string', () => {
      const result = parseDate('2024-01-15T10:30:00.000Z');
      assert.ok(result instanceof Date);
      assert.strictEqual(result.toISOString(), '2024-01-15T10:30:00.000Z');
    });

    it('should parse simple date string', () => {
      const result = parseDate('2024-01-15');
      assert.ok(result instanceof Date);
      assert.strictEqual(result.getFullYear(), 2024);
      assert.strictEqual(result.getMonth(), 0);  // January = 0
      assert.strictEqual(result.getDate(), 15);
    });

    it('should parse date with time', () => {
      const result = parseDate('2024-06-20 14:30:00');
      assert.ok(result instanceof Date);
      assert.strictEqual(result.getFullYear(), 2024);
    });
  });

  describe('formatError', () => {
    it('should format error message without details', () => {
      const result = formatError('Something went wrong');
      assert.deepStrictEqual(result, { error: 'Something went wrong' });
    });

    it('should format error message with details', () => {
      const details = [{ field: 'email', message: 'Invalid format' }];
      const result = formatError('Validation failed', details);
      assert.deepStrictEqual(result, {
        error: 'Validation failed',
        details: [{ field: 'email', message: 'Invalid format' }]
      });
    });

    it('should not include details when null', () => {
      const result = formatError('Error', null);
      assert.strictEqual(result.details, undefined);
    });

    it('should handle empty details array', () => {
      const result = formatError('Error', []);
      assert.deepStrictEqual(result, { error: 'Error', details: [] });
    });
  });

  describe('formatSuccess', () => {
    it('should format success response without message', () => {
      const data = { id: 1, name: 'Test' };
      const result = formatSuccess(data);
      assert.deepStrictEqual(result, { data: { id: 1, name: 'Test' } });
    });

    it('should format success response with message', () => {
      const data = { id: 1 };
      const result = formatSuccess(data, 'Created successfully');
      assert.deepStrictEqual(result, {
        data: { id: 1 },
        message: 'Created successfully'
      });
    });

    it('should not include message when null', () => {
      const result = formatSuccess({ id: 1 }, null);
      assert.strictEqual(result.message, undefined);
    });

    it('should handle array data', () => {
      const data = [1, 2, 3];
      const result = formatSuccess(data);
      assert.deepStrictEqual(result, { data: [1, 2, 3] });
    });

    it('should handle primitive data', () => {
      const result = formatSuccess(42, 'Number response');
      assert.deepStrictEqual(result, { data: 42, message: 'Number response' });
    });

    it('should handle null data', () => {
      const result = formatSuccess(null);
      assert.deepStrictEqual(result, { data: null });
    });
  });
});
