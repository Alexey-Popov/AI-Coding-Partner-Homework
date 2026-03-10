const request = require('supertest');
const app = require('../src/index');
const { resetStore, getAllTickets } = require('../src/data/store');
const fs = require('fs');
const path = require('path');

describe('Integration Tests', () => {
  beforeEach(() => {
    resetStore();
  });

  describe('Complete Ticket Lifecycle', () => {
    test('creates, updates, resolves, and closes a ticket', async () => {
      // Create ticket
      const createRes = await request(app)
        .post('/tickets')
        .send({
          customer_id: 'CUST-001',
          customer_email: 'test@email.com',
          customer_name: 'John Doe',
          subject: 'Test Issue',
          description: 'This is a test issue that needs to be resolved.'
        });

      expect(createRes.status).toBe(201);
      const ticketId = createRes.body.id;
      expect(createRes.body.status).toBe('new');

      // Update to in_progress
      const updateRes = await request(app)
        .put(`/tickets/${ticketId}`)
        .send({ status: 'in_progress', assigned_to: 'agent-1' });

      expect(updateRes.status).toBe(200);
      expect(updateRes.body.status).toBe('in_progress');
      expect(updateRes.body.assigned_to).toBe('agent-1');

      // Resolve ticket
      const resolveRes = await request(app)
        .put(`/tickets/${ticketId}`)
        .send({ status: 'resolved' });

      expect(resolveRes.status).toBe(200);
      expect(resolveRes.body.status).toBe('resolved');
      expect(resolveRes.body.resolved_at).toBeDefined();

      // Close ticket
      const closeRes = await request(app)
        .put(`/tickets/${ticketId}`)
        .send({ status: 'closed' });

      expect(closeRes.status).toBe(200);
      expect(closeRes.body.status).toBe('closed');
    });
  });

  describe('Bulk Import with Auto-Classification', () => {
    test('imports CSV and auto-classifies tickets', async () => {
      const csvContent = `customer_id,customer_email,customer_name,subject,description
CUST-001,test1@email.com,John,Cannot login,I forgot my password and cannot access my account urgently
CUST-002,test2@email.com,Jane,Refund request,Please refund my last payment as it was charged twice
CUST-003,test3@email.com,Bob,Feature request,Would be nice to add dark mode feature someday`;

      const res = await request(app)
        .post('/tickets/import?format=csv&autoClassify=true')
        .attach('file', Buffer.from(csvContent), 'tickets.csv');

      expect(res.status).toBe(200);
      expect(res.body.summary.successful).toBe(3);

      // Verify classification
      const tickets = getAllTickets();
      const loginTicket = tickets.find(t => t.subject === 'Cannot login');
      const refundTicket = tickets.find(t => t.subject === 'Refund request');

      expect(loginTicket.category).toBe('account_access');
      expect(loginTicket.priority).toBe('urgent');
      expect(refundTicket.category).toBe('billing_question');
    });
  });

  describe('Combined Filtering', () => {
    beforeEach(async () => {
      // Create multiple tickets with different categories and priorities
      const tickets = [
        { customer_id: 'C1', customer_email: 'a@b.com', customer_name: 'A', subject: 'Test 1', description: 'Description one here', category: 'technical_issue', priority: 'high' },
        { customer_id: 'C2', customer_email: 'b@b.com', customer_name: 'B', subject: 'Test 2', description: 'Description two here', category: 'technical_issue', priority: 'medium' },
        { customer_id: 'C3', customer_email: 'c@b.com', customer_name: 'C', subject: 'Test 3', description: 'Description three here', category: 'billing_question', priority: 'high' },
        { customer_id: 'C4', customer_email: 'd@b.com', customer_name: 'D', subject: 'Test 4', description: 'Description four here', category: 'billing_question', priority: 'low' }
      ];

      for (const ticket of tickets) {
        await request(app).post('/tickets').send(ticket);
      }
    });

    test('filters by category and priority combined', async () => {
      const res = await request(app).get('/tickets?category=technical_issue&priority=high');

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(1);
      expect(res.body[0].category).toBe('technical_issue');
      expect(res.body[0].priority).toBe('high');
    });
  });

  describe('Concurrent Operations', () => {
    test('handles 20+ simultaneous ticket creation requests', async () => {
      const createRequests = [];

      for (let i = 0; i < 25; i++) {
        createRequests.push(
          request(app)
            .post('/tickets')
            .send({
              customer_id: `CUST-${i}`,
              customer_email: `test${i}@email.com`,
              customer_name: `User ${i}`,
              subject: `Concurrent Test ${i}`,
              description: `This is concurrent test ticket number ${i} for testing.`
            })
        );
      }

      const results = await Promise.all(createRequests);

      // All should succeed
      const successCount = results.filter(r => r.status === 201).length;
      expect(successCount).toBe(25);

      // Verify all tickets were created
      const tickets = getAllTickets();
      expect(tickets.length).toBe(25);
    });
  });

  describe('Error Handling', () => {
    test('handles malformed import gracefully', async () => {
      const res = await request(app)
        .post('/tickets/import?format=json')
        .attach('file', Buffer.from('{ invalid json }'), 'bad.json');

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Import failed');
    });
  });
});
