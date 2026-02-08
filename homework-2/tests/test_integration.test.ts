import request from 'supertest';
import app from '../src/app';
import { ticketRepository } from '../src/repositories/ticket.repository';
import path from 'path';

describe('Integration Tests - End-to-End Workflows', () => {
    beforeEach(() => {
        ticketRepository.clear();
    });

    it('should complete full ticket lifecycle workflow', async () => {
        const createResponse = await request(app)
            .post('/tickets')
            .send({
                customer_id: 'cust-lifecycle',
                customer_email: 'lifecycle@example.com',
                customer_name: 'Lifecycle Test',
                subject: 'Test complete workflow',
                description: 'This ticket will go through the complete lifecycle from creation to resolution.',
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            });

        expect(createResponse.status).toBe(201);
        const ticketId = createResponse.body.data.id;
        expect(createResponse.body.data.status).toBe('new');

        const updateToInProgress = await request(app)
            .put(`/tickets/${ticketId}`)
            .send({
                status: 'in_progress',
                assigned_to: 'agent-test'
            });

        expect(updateToInProgress.status).toBe(200);
        expect(updateToInProgress.body.data.status).toBe('in_progress');
        expect(updateToInProgress.body.data.assigned_to).toBe('agent-test');

        const updateToResolved = await request(app)
            .put(`/tickets/${ticketId}`)
            .send({
                status: 'resolved'
            });

        expect(updateToResolved.status).toBe(200);
        expect(updateToResolved.body.data.status).toBe('resolved');
        expect(updateToResolved.body.data.resolved_at).toBeDefined();

        const getTicket = await request(app).get(`/tickets/${ticketId}`);
        expect(getTicket.status).toBe(200);
        expect(getTicket.body.data.status).toBe('resolved');
    });

    it('should bulk import with auto-classification and verify results', async () => {
        const csvPath = path.join(__dirname, 'fixtures', 'valid_tickets.csv');

        const importResponse = await request(app)
            .post('/tickets/import')
            .attach('file', csvPath);

        expect(importResponse.status).toBe(200);
        expect(importResponse.body.imported).toBeGreaterThan(0);

        const getAllResponse = await request(app).get('/tickets');
        expect(getAllResponse.status).toBe(200);
        expect(getAllResponse.body.data.length).toBeGreaterThanOrEqual(importResponse.body.imported);

        getAllResponse.body.data.forEach((ticket: any) => {
            expect(ticket).toHaveProperty('category');
            expect(ticket).toHaveProperty('priority');
        });
    });

    it('should handle concurrent ticket operations', async () => {
        const requests = Array.from({ length: 20 }, (_, i) =>
            request(app)
                .post('/tickets')
                .send({
                    customer_id: `cust-concurrent-${i}`,
                    customer_email: `user${i}@example.com`,
                    customer_name: `User ${i}`,
                    subject: `Concurrent ticket ${i}`,
                    description: `This is a concurrent test ticket number ${i} with sufficient description length.`,
                    metadata: {
                        source: 'api',
                        browser: null,
                        device_type: 'desktop'
                    }
                })
        );

        const responses = await Promise.all(requests);

        responses.forEach(response => {
            expect(response.status).toBe(201);
            expect(response.body.data).toHaveProperty('id');
        });

        const getAllResponse = await request(app).get('/tickets');
        expect(getAllResponse.body.data.length).toBe(20);

        const uniqueIds = new Set(getAllResponse.body.data.map((t: any) => t.id));
        expect(uniqueIds.size).toBe(20);
    });

    it('should filter tickets by combined category and priority', async () => {
        await request(app).post('/tickets').send({
            customer_id: 'cust-001',
            customer_email: 'urgent-login@example.com',
            customer_name: 'Urgent User',
            subject: 'Cannot login - critical issue',
            description: 'Cannot access account. This is urgent and blocking all work.',
            metadata: { source: 'web_form', browser: 'Chrome', device_type: 'desktop' }
        });

        await request(app).post('/tickets').send({
            customer_id: 'cust-002',
            customer_email: 'minor-feature@example.com',
            customer_name: 'Feature User',
            subject: 'Minor feature suggestion',
            description: 'Small cosmetic improvement suggestion for the UI layout.',
            metadata: { source: 'api', browser: null, device_type: 'mobile' }
        });

        await request(app).post('/tickets').send({
            customer_id: 'cust-003',
            customer_email: 'urgent-billing@example.com',
            customer_name: 'Billing User',
            subject: 'Critical billing error',
            description: 'Double charged urgently need refund. This is critical for our business.',
            metadata: { source: 'email', browser: null, device_type: 'desktop' }
        });

        const urgentTickets = await request(app).get('/tickets?priority=urgent');
        expect(urgentTickets.status).toBe(200);
        expect(urgentTickets.body.data.length).toBeGreaterThan(0);
        urgentTickets.body.data.forEach((ticket: any) => {
            expect(ticket.priority).toBe('urgent');
        });

        const accountAccessTickets = await request(app).get('/tickets?category=account_access');
        expect(accountAccessTickets.status).toBe(200);
        if (accountAccessTickets.body.data.length > 0) {
            accountAccessTickets.body.data.forEach((ticket: any) => {
                expect(ticket.category).toBe('account_access');
            });
        }
    });

    it('should maintain data integrity through multiple updates', async () => {
        const createResponse = await request(app)
            .post('/tickets')
            .send({
                customer_id: 'cust-integrity',
                customer_email: 'integrity@example.com',
                customer_name: 'Integrity Test',
                subject: 'Data integrity test ticket',
                description: 'This ticket will be updated multiple times to test data integrity.',
                metadata: {
                    source: 'web_form',
                    browser: 'Firefox',
                    device_type: 'desktop'
                }
            });

        const ticketId = createResponse.body.data.id;
        const originalEmail = createResponse.body.data.customer_email;
        const originalCreatedAt = createResponse.body.data.created_at;

        await request(app).put(`/tickets/${ticketId}`).send({ status: 'in_progress' });
        await request(app).put(`/tickets/${ticketId}`).send({ assigned_to: 'agent-1' });
        await request(app).put(`/tickets/${ticketId}`).send({ status: 'waiting_customer' });
        await request(app).put(`/tickets/${ticketId}`).send({ assigned_to: 'agent-2' });

        const finalState = await request(app).get(`/tickets/${ticketId}`);

        expect(finalState.body.data.customer_email).toBe(originalEmail);
        expect(finalState.body.data.created_at).toBe(originalCreatedAt);
        expect(finalState.body.data.status).toBe('waiting_customer');
        expect(finalState.body.data.assigned_to).toBe('agent-2');
        expect(finalState.body.data.updated_at).not.toBe(originalCreatedAt);
    });
});
