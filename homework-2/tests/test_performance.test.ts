import request from 'supertest';
import app from '../src/app';
import { ticketRepository } from '../src/repositories/ticket.repository';
import path from 'path';

describe('Performance Benchmarks', () => {
    beforeEach(() => {
        ticketRepository.clear();
    });

    it('should handle 100 ticket creations within reasonable time', async () => {
        const startTime = Date.now();
        const requests = Array.from({ length: 100 }, (_, i) =>
            request(app)
                .post('/tickets')
                .send({
                    customer_id: `cust-perf-${i}`,
                    customer_email: `perf${i}@example.com`,
                    customer_name: `Performance User ${i}`,
                    subject: `Performance test ticket ${i}`,
                    description: `This is a performance test ticket number ${i} with sufficient description content.`,
                    metadata: {
                        source: 'api',
                        browser: null,
                        device_type: 'desktop'
                    }
                })
        );

        const responses = await Promise.all(requests);
        const endTime = Date.now();
        const duration = endTime - startTime;

        expect(responses.length).toBe(100);
        responses.forEach(response => {
            expect(response.status).toBe(201);
            expect(response.body.data).toHaveProperty('id');
        });

        console.log(`Created 100 tickets in ${duration}ms (avg: ${duration / 100}ms per ticket)`);
        expect(duration).toBeLessThan(10000);
    });

    it('should retrieve paginated results efficiently', async () => {
        await Promise.all(
            Array.from({ length: 200 }, (_, i) =>
                request(app)
                    .post('/tickets')
                    .send({
                        customer_id: `cust-page-${i}`,
                        customer_email: `page${i}@example.com`,
                        customer_name: `Page User ${i}`,
                        subject: `Pagination test ${i}`,
                        description: `Pagination test ticket with enough description content to pass validation.`,
                        metadata: {
                            source: 'api',
                            browser: null,
                            device_type: 'desktop'
                        }
                    })
            )
        );

        const startTime = Date.now();
        const response = await request(app).get('/tickets?limit=50&offset=0');
        const endTime = Date.now();
        const duration = endTime - startTime;

        expect(response.status).toBe(200);
        expect(response.body.data.length).toBe(50);

        console.log(`Retrieved 50 tickets from 200 in ${duration}ms`);
        expect(duration).toBeLessThan(1000);
    });

    it('should filter large dataset efficiently', async () => {
        await Promise.all(
            Array.from({ length: 150 }, (_, i) =>
                request(app)
                    .post('/tickets')
                    .send({
                        customer_id: `cust-filter-${i}`,
                        customer_email: `filter${i}@example.com`,
                        customer_name: `Filter User ${i}`,
                        subject: i % 3 === 0 ? 'Cannot login - urgent' : 'Minor suggestion',
                        description: `Filter test ticket ${i} with adequate description length for validation.`,
                        tags: i % 3 === 0 ? ['login', 'urgent'] : ['feature'],
                        metadata: {
                            source: 'web_form',
                            browser: 'Chrome',
                            device_type: 'desktop'
                        }
                    })
            )
        );

        const startTime = Date.now();
        const response = await request(app).get('/tickets?priority=high');
        const endTime = Date.now();
        const duration = endTime - startTime;

        expect(response.status).toBe(200);
        expect(response.body.data.length).toBeGreaterThan(0);

        console.log(`Filtered ${response.body.data.length} high priority tickets from 150 total in ${duration}ms`);
        expect(duration).toBeLessThan(500);
    });

    it('should handle bulk CSV import efficiently', async () => {
        const csvPath = path.join(__dirname, 'fixtures', 'valid_tickets.csv');

        const startTime = Date.now();
        const response = await request(app)
            .post('/tickets/import')
            .attach('file', csvPath);
        const endTime = Date.now();
        const duration = endTime - startTime;

        expect(response.status).toBe(200);
        expect(response.body.imported).toBeGreaterThan(0);

        console.log(`Imported ${response.body.imported} tickets from CSV in ${duration}ms`);
        expect(duration).toBeLessThan(5000);
    });

    it('should update tickets with minimal latency', async () => {
        const createResponse = await request(app)
            .post('/tickets')
            .send({
                customer_id: 'cust-update-perf',
                customer_email: 'updateperf@example.com',
                customer_name: 'Update Perf',
                subject: 'Update performance test',
                description: 'Testing update operation performance with sufficient description length.',
                metadata: {
                    source: 'api',
                    browser: null,
                    device_type: 'desktop'
                }
            });

        const ticketId = createResponse.body.data.id;

        const updateRequests = Array.from({ length: 10 }, (_, i) => ({
            status: i % 2 === 0 ? 'in_progress' : 'waiting_customer',
        }));

        const startTime = Date.now();
        for (const update of updateRequests) {
            const response = await request(app)
                .put(`/tickets/${ticketId}`)
                .send(update);
            expect(response.status).toBe(200);
        }
        const endTime = Date.now();
        const duration = endTime - startTime;

        console.log(`Performed 10 sequential updates in ${duration}ms (avg: ${duration / 10}ms per update)`);
        expect(duration).toBeLessThan(2000);
    });
});
