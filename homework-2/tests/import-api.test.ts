import request from 'supertest';
import * as path from 'path';
import app from '../src/app';

describe('POST /tickets/import', () => {
  const fixturesPath = path.join(__dirname, 'fixtures');

  it('should import valid CSV file', async () => {
    const filePath = path.join(fixturesPath, 'valid-tickets.csv');

    const response = await request(app)
      .post('/tickets/import')
      .attach('file', filePath);

    expect(response.status).toBe(200);
    expect(response.body.success).toBe(true);
    expect(response.body.imported).toBe(3);
    expect(response.body.failed).toBe(0);
    expect(response.body.tickets).toHaveLength(3);
    expect(response.body.tickets[0]).toHaveProperty('id');
    expect(response.body.tickets[0]).toHaveProperty('category');
    expect(response.body.tickets[0]).toHaveProperty('priority');
  });

  it('should reject request without file', async () => {
    const response = await request(app).post('/tickets/import');

    expect(response.status).toBe(400);
    expect(response.body.success).toBe(false);
    expect(response.body.error).toBe('No file uploaded');
  });

  it('should reject non-CSV files', async () => {
    const response = await request(app)
      .post('/tickets/import')
      .attach('file', Buffer.from('test'), 'test.txt');

    expect(response.status).toBe(400);
    expect(response.body.error).toBe('Invalid file type');
  });

  it('should handle CSV with validation errors', async () => {
    const filePath = path.join(fixturesPath, 'invalid-email.csv');

    const response = await request(app)
      .post('/tickets/import')
      .attach('file', filePath);

    expect(response.status).toBe(400);
    expect(response.body.success).toBe(false);
    expect(response.body.imported).toBe(4);
    expect(response.body.failed).toBe(1);
    expect(response.body.errors.length).toBeGreaterThan(0);
  });

  it('should handle CSV with missing headers', async () => {
    const filePath = path.join(fixturesPath, 'missing-fields.csv');

    const response = await request(app)
      .post('/tickets/import')
      .attach('file', filePath);

    expect(response.status).toBe(400);
    expect(response.body.success).toBe(false);
    expect(response.body.errors[0]?.field).toBe('headers');
  });

  it('should handle empty CSV file', async () => {
    const filePath = path.join(fixturesPath, 'empty.csv');

    const response = await request(app)
      .post('/tickets/import')
      .attach('file', filePath);

    expect(response.status).toBe(200);
    expect(response.body.imported).toBe(0);
  });

  it('should import large CSV file', async () => {
    const filePath = path.join(fixturesPath, 'large-tickets.csv');

    const response = await request(app)
      .post('/tickets/import')
      .attach('file', filePath);

    expect(response.status).toBe(200);
    expect(response.body.success).toBe(true);
    expect(response.body.imported).toBe(100);
    expect(response.body.tickets).toHaveLength(100);
  });

  it('should verify imported tickets are accessible via GET', async () => {
    const filePath = path.join(fixturesPath, 'valid-tickets.csv');

    const importResponse = await request(app)
      .post('/tickets/import')
      .attach('file', filePath);

    const ticketId = importResponse.body.tickets[0]?.id;
    expect(ticketId).toBeDefined();

    const getResponse = await request(app).get(`/tickets/${ticketId}`);

    expect(getResponse.status).toBe(200);
    expect(getResponse.body.data.id).toBe(ticketId);
  });

  it('should import valid JSON file', async () => {
    const filePath = path.join(fixturesPath, 'valid-tickets.json');

    const response = await request(app)
      .post('/tickets/import')
      .attach('file', filePath);

    expect(response.status).toBe(200);
    expect(response.body.success).toBe(true);
    expect(response.body.format).toBe('JSON');
    expect(response.body.imported).toBe(3);
    expect(response.body.failed).toBe(0);
    expect(response.body.tickets).toHaveLength(3);
  });

  it('should import valid XML file', async () => {
    const filePath = path.join(fixturesPath, 'valid-tickets.xml');

    const response = await request(app)
      .post('/tickets/import')
      .attach('file', filePath);

    expect(response.status).toBe(200);
    expect(response.body.success).toBe(true);
    expect(response.body.format).toBe('XML');
    expect(response.body.imported).toBe(3);
    expect(response.body.failed).toBe(0);
    expect(response.body.tickets).toHaveLength(3);
  });

  it('should detect invalid email in JSON', async () => {
    const filePath = path.join(fixturesPath, 'invalid-email.json');

    const response = await request(app)
      .post('/tickets/import')
      .attach('file', filePath);

    expect(response.status).toBe(400);
    expect(response.body.success).toBe(false);
    expect(response.body.imported).toBe(1);
    expect(response.body.failed).toBe(1);
  });

  it('should reject malformed JSON', async () => {
    const filePath = path.join(fixturesPath, 'malformed.json');

    const response = await request(app)
      .post('/tickets/import')
      .attach('file', filePath);

    expect(response.status).toBe(400);
    expect(response.body.success).toBe(false);
  });
});
