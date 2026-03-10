const request = require('supertest');
const app = require('../src/index');
const { resetStore } = require('../src/data/store');

describe('Ticket API Endpoints', () => {
  beforeEach(() => {
    resetStore();
  });

  const validTicket = {
    customer_id: 'CUST-001',
    customer_email: 'test@email.com',
    customer_name: 'John Doe',
    subject: 'Test Subject',
    description: 'This is a valid description with enough characters for testing.'
  };

  describe('POST /tickets', () => {
    test('creates a new ticket with valid data', async () => {
      const res = await request(app)
        .post('/tickets')
        .send(validTicket);

      expect(res.status).toBe(201);
      expect(res.body.id).toBeDefined();
      expect(res.body.customer_email).toBe('test@email.com');
      expect(res.body.status).toBe('new');
    });

    test('returns 400 for invalid ticket data', async () => {
      const res = await request(app)
        .post('/tickets')
        .send({ customer_id: 'CUST-001' });

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation failed');
      expect(res.body.details.length).toBeGreaterThan(0);
    });

    test('creates ticket with auto-classify flag', async () => {
      const res = await request(app)
        .post('/tickets?autoClassify=true')
        .send({
          ...validTicket,
          subject: 'Cannot login to my account',
          description: 'I forgot my password and cannot access my account. Please help reset it.'
        });

      expect(res.status).toBe(201);
      expect(res.body.category).toBe('account_access');
    });
  });

  describe('GET /tickets', () => {
    beforeEach(async () => {
      await request(app).post('/tickets').send(validTicket);
      await request(app).post('/tickets').send({
        ...validTicket,
        customer_id: 'CUST-002',
        category: 'billing_question',
        priority: 'high'
      });
    });

    test('returns all tickets', async () => {
      const res = await request(app).get('/tickets');

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(2);
    });

    test('filters tickets by category', async () => {
      const res = await request(app).get('/tickets?category=billing_question');

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(1);
      expect(res.body[0].category).toBe('billing_question');
    });

    test('filters tickets by priority', async () => {
      const res = await request(app).get('/tickets?priority=high');

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(1);
    });
  });

  describe('GET /tickets/:id', () => {
    test('returns ticket by ID', async () => {
      const createRes = await request(app).post('/tickets').send(validTicket);
      const ticketId = createRes.body.id;

      const res = await request(app).get(`/tickets/${ticketId}`);

      expect(res.status).toBe(200);
      expect(res.body.id).toBe(ticketId);
    });

    test('returns 404 for non-existent ticket', async () => {
      const res = await request(app).get('/tickets/non-existent-id');

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Not found');
    });
  });

  describe('PUT /tickets/:id', () => {
    test('updates ticket successfully', async () => {
      const createRes = await request(app).post('/tickets').send(validTicket);
      const ticketId = createRes.body.id;

      const res = await request(app)
        .put(`/tickets/${ticketId}`)
        .send({ status: 'in_progress', priority: 'high' });

      expect(res.status).toBe(200);
      expect(res.body.status).toBe('in_progress');
      expect(res.body.priority).toBe('high');
    });

    test('returns 404 when updating non-existent ticket', async () => {
      const res = await request(app)
        .put('/tickets/non-existent')
        .send({ status: 'closed' });

      expect(res.status).toBe(404);
    });
  });

  describe('DELETE /tickets/:id', () => {
    test('deletes ticket successfully', async () => {
      const createRes = await request(app).post('/tickets').send(validTicket);
      const ticketId = createRes.body.id;

      const res = await request(app).delete(`/tickets/${ticketId}`);
      expect(res.status).toBe(204);

      const getRes = await request(app).get(`/tickets/${ticketId}`);
      expect(getRes.status).toBe(404);
    });
  });

  describe('POST /tickets/:id/auto-classify', () => {
    test('auto-classifies ticket and returns result', async () => {
      const createRes = await request(app).post('/tickets').send({
        ...validTicket,
        subject: 'Payment failed',
        description: 'I tried to make a payment but my card was declined. Please help with this billing issue.'
      });
      const ticketId = createRes.body.id;

      const res = await request(app).post(`/tickets/${ticketId}/auto-classify`);

      expect(res.status).toBe(200);
      expect(res.body.category).toBe('billing_question');
      expect(res.body.category_confidence).toBeGreaterThan(0);
      expect(res.body.reasoning).toBeDefined();
    });
  });
});
