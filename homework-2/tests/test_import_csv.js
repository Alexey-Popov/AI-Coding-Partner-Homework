const { parseCSV } = require('../src/services/importers/csvImporter');
const { importTickets } = require('../src/services/importers');
const { resetStore, getAllTickets } = require('../src/data/store');
const fs = require('fs');
const path = require('path');

describe('CSV Import', () => {
  beforeEach(() => {
    resetStore();
  });

  describe('parseCSV', () => {
    test('parses valid CSV content', () => {
      const csv = `customer_id,customer_email,customer_name,subject,description
CUST-001,test@email.com,John Doe,Test Subject,This is a valid description for testing purposes.`;

      const records = parseCSV(csv);

      expect(records.length).toBe(1);
      expect(records[0].data.customer_id).toBe('CUST-001');
      expect(records[0].data.customer_email).toBe('test@email.com');
      expect(records[0].rowNumber).toBe(2);
    });

    test('handles tags as comma-separated values in quoted field', () => {
      const csv = `customer_id,customer_email,customer_name,subject,description,tags
CUST-001,test@email.com,John Doe,Test Subject,Valid description here,"tag1,tag2,tag3"`;

      const records = parseCSV(csv);
      expect(records[0].data.tags).toEqual(['tag1', 'tag2', 'tag3']);
    });

    test('handles metadata fields', () => {
      const csv = `customer_id,customer_email,customer_name,subject,description,source,browser,device_type
CUST-001,test@email.com,John Doe,Test Subject,Valid description here,web_form,Chrome,desktop`;

      const records = parseCSV(csv);
      expect(records[0].data.metadata.source).toBe('web_form');
      expect(records[0].data.metadata.browser).toBe('Chrome');
    });

    test('handles empty CSV', () => {
      const csv = `customer_id,customer_email,customer_name,subject,description`;
      const records = parseCSV(csv);
      expect(records.length).toBe(0);
    });
  });

  describe('importTickets CSV', () => {
    test('imports valid CSV file successfully', async () => {
      const csvContent = fs.readFileSync(
        path.join(__dirname, '../data/sample_tickets.csv'),
        'utf-8'
      );

      const result = await importTickets(csvContent, 'csv');

      expect(result.total).toBe(50);
      expect(result.successful).toBe(50);
      expect(result.failed).toBe(0);
    });

    test('reports validation errors for invalid CSV', async () => {
      const csvContent = fs.readFileSync(
        path.join(__dirname, '../data/invalid_tickets.csv'),
        'utf-8'
      );

      const result = await importTickets(csvContent, 'csv');

      expect(result.failed).toBeGreaterThan(0);
      expect(result.errors.length).toBeGreaterThan(0);
    });
  });
});
