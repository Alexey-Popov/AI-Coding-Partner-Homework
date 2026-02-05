const { parseJSON } = require('../src/services/importers/jsonImporter');
const { importTickets } = require('../src/services/importers');
const { resetStore } = require('../src/data/store');
const fs = require('fs');
const path = require('path');

describe('JSON Import', () => {
  beforeEach(() => {
    resetStore();
  });

  describe('parseJSON', () => {
    test('parses array of tickets', () => {
      const json = JSON.stringify([
        {
          customer_id: 'CUST-001',
          customer_email: 'test@email.com',
          customer_name: 'John Doe',
          subject: 'Test',
          description: 'Valid description here'
        }
      ]);

      const records = parseJSON(json);
      expect(records.length).toBe(1);
      expect(records[0].data.customer_id).toBe('CUST-001');
    });

    test('parses object with tickets property', () => {
      const json = JSON.stringify({
        tickets: [
          {
            customer_id: 'CUST-001',
            customer_email: 'test@email.com',
            customer_name: 'John Doe',
            subject: 'Test',
            description: 'Valid description'
          }
        ]
      });

      const records = parseJSON(json);
      expect(records.length).toBe(1);
    });

    test('throws error for malformed JSON', () => {
      expect(() => parseJSON('{ invalid json')).toThrow('JSON parsing error');
    });

    test('handles empty array', () => {
      const records = parseJSON('[]');
      expect(records.length).toBe(0);
    });
  });

  describe('importTickets JSON', () => {
    test('imports valid JSON file successfully', async () => {
      const jsonContent = fs.readFileSync(
        path.join(__dirname, '../data/sample_tickets.json'),
        'utf-8'
      );

      const result = await importTickets(jsonContent, 'json');

      expect(result.total).toBe(20);
      expect(result.successful).toBe(20);
      expect(result.failed).toBe(0);
    });
  });
});
