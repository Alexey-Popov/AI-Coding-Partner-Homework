const request = require('supertest');
const app = require('../src/index');
const { resetStore, getAllTickets, addTicket } = require('../src/data/store');
const { createTicket } = require('../src/models/ticket');
const { autoClassify } = require('../src/services/classifier');

describe('Performance Tests', () => {
  beforeEach(() => {
    resetStore();
  });

  describe('Bulk Operations', () => {
    test('creates 100 tickets in under 2 seconds', async () => {
      const startTime = Date.now();

      const promises = [];
      for (let i = 0; i < 100; i++) {
        promises.push(
          request(app)
            .post('/tickets')
            .send({
              customer_id: `CUST-${i}`,
              customer_email: `perf${i}@test.com`,
              customer_name: `User ${i}`,
              subject: `Performance Test ${i}`,
              description: `This is a performance test ticket number ${i} to measure throughput.`
            })
        );
      }

      await Promise.all(promises);
      const elapsed = Date.now() - startTime;

      expect(elapsed).toBeLessThan(2000);
      expect(getAllTickets().length).toBe(100);
    });

    test('retrieves 1000 tickets in under 500ms', async () => {
      // Pre-populate with tickets
      for (let i = 0; i < 1000; i++) {
        const ticket = createTicket({
          customer_id: `CUST-${i}`,
          customer_email: `bulk${i}@test.com`,
          customer_name: `Bulk User ${i}`,
          subject: `Bulk Test ${i}`,
          description: `Bulk test ticket for performance testing.`
        });
        addTicket(ticket);
      }

      const startTime = Date.now();
      const res = await request(app).get('/tickets');
      const elapsed = Date.now() - startTime;

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(1000);
      expect(elapsed).toBeLessThan(500);
    });
  });

  describe('Classification Performance', () => {
    test('classifies 100 tickets in under 100ms', () => {
      const tickets = [];
      for (let i = 0; i < 100; i++) {
        tickets.push({
          id: `perf-${i}`,
          subject: 'Cannot login to my account',
          description: 'I forgot my password and need urgent help to reset it immediately.'
        });
      }

      const startTime = Date.now();
      tickets.forEach(t => autoClassify(t));
      const elapsed = Date.now() - startTime;

      expect(elapsed).toBeLessThan(100);
    });
  });

  describe('Filtering Performance', () => {
    beforeEach(() => {
      // Pre-populate with 500 tickets of various categories
      const categories = ['account_access', 'technical_issue', 'billing_question', 'feature_request', 'bug_report'];
      const priorities = ['urgent', 'high', 'medium', 'low'];

      for (let i = 0; i < 500; i++) {
        const ticket = createTicket({
          customer_id: `CUST-${i}`,
          customer_email: `filter${i}@test.com`,
          customer_name: `Filter User ${i}`,
          subject: `Filter Test ${i}`,
          description: `Filter test ticket for performance testing purposes.`,
          category: categories[i % 5],
          priority: priorities[i % 4]
        });
        addTicket(ticket);
      }
    });

    test('filters 500 tickets by category in under 100ms', async () => {
      const startTime = Date.now();
      const res = await request(app).get('/tickets?category=technical_issue');
      const elapsed = Date.now() - startTime;

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(100); // 500/5 categories
      expect(elapsed).toBeLessThan(100);
    });

    test('filters 500 tickets by multiple criteria in under 100ms', async () => {
      const startTime = Date.now();
      const res = await request(app).get('/tickets?category=billing_question&priority=high');
      const elapsed = Date.now() - startTime;

      expect(res.status).toBe(200);
      expect(elapsed).toBeLessThan(100);
    });
  });
});
