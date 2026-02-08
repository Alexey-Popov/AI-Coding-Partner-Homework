import request from 'supertest';
import app from '../src/app';

describe('Tickets API Routes', () => {
  let createdTicketId: string;

  describe('Health Check', () => {
    it('should return health status', async () => {
      const response = await request(app).get('/health');

      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      expect(response.body.message).toBe('Server is running');
    });
  });

  const validTicket = {
    customer_id: 'cust_123',
    customer_email: 'test@example.com',
    customer_name: 'John Doe',
    subject: 'Login issue',
    description: 'Cannot access my account after password reset',
    metadata: {
      source: 'web_form',
      browser: 'Chrome 120',
      device_type: 'desktop',
    },
  };

  describe('POST /tickets', () => {
    it('should create a new ticket', async () => {
      const response = await request(app).post('/tickets').send(validTicket);

      expect(response.status).toBe(201);
      expect(response.body.success).toBe(true);
      expect(response.body.data).toHaveProperty('id');
      expect(response.body.data.status).toBe('new');
      createdTicketId = response.body.data.id;
    });

    it('should reject invalid email', async () => {
      const invalid = { ...validTicket, customer_email: 'bad-email' };
      const response = await request(app).post('/tickets').send(invalid);

      expect(response.status).toBe(400);
      expect(response.body.success).toBe(false);
    });
  });

  describe('GET /tickets', () => {
    it('should list all tickets', async () => {
      const response = await request(app).get('/tickets');

      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      expect(Array.isArray(response.body.data)).toBe(true);
    });

    it('should filter by status', async () => {
      const response = await request(app).get('/tickets?status=new');

      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
    });
  });

  describe('GET /tickets/:id', () => {
    it('should get a ticket by ID', async () => {
      const response = await request(app).get(`/tickets/${createdTicketId}`);

      expect(response.status).toBe(200);
      expect(response.body.data.id).toBe(createdTicketId);
    });

    it('should return 404 for non-existent ticket', async () => {
      const fakeId = '00000000-0000-0000-0000-000000000000';
      const response = await request(app).get(`/tickets/${fakeId}`);

      expect(response.status).toBe(404);
    });

    it('should reject invalid UUID', async () => {
      const response = await request(app).get('/tickets/invalid-uuid');

      expect(response.status).toBe(400);
    });
  });

  describe('PUT /tickets/:id', () => {
    it('should update a ticket', async () => {
      const update = { subject: 'Updated subject' };
      const response = await request(app)
        .put(`/tickets/${createdTicketId}`)
        .send(update);

      expect(response.status).toBe(200);
      expect(response.body.data.subject).toBe('Updated subject');
    });

    it('should return 404 for non-existent ticket', async () => {
      const fakeId = '00000000-0000-0000-0000-000000000000';
      const response = await request(app).put(`/tickets/${fakeId}`).send({});

      expect(response.status).toBe(404);
    });
  });

  describe('DELETE /tickets/:id', () => {
    it('should delete a ticket', async () => {
      const response = await request(app).delete(`/tickets/${createdTicketId}`);

      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
    });

    it('should return 404 when deleting non-existent ticket', async () => {
      const fakeId = '00000000-0000-0000-0000-000000000000';
      const response = await request(app).delete(`/tickets/${fakeId}`);

      expect(response.status).toBe(404);
    });

    it('should reject invalid UUID for delete', async () => {
      const response = await request(app).delete('/tickets/not-a-uuid');

      expect(response.status).toBe(400);
    });
  });

  describe('Error Handling', () => {
    it('should return 404 for undefined routes', async () => {
      const response = await request(app).get('/nonexistent');

      expect(response.status).toBe(404);
      expect(response.body.error).toBe('Route not found');
    });

    it('should validate required fields on ticket creation', async () => {
      const response = await request(app).post('/tickets').send({
        customer_email: 'test@example.com',
      });

      expect(response.status).toBe(400);
      expect(response.body.success).toBe(false);
    });
  });
});
