import request from 'supertest';
import app from '../src/app';
import { ticketRepository } from '../src/repositories/ticket.repository';

describe('Ticket API Endpoints', () => {
    beforeEach(() => {
        ticketRepository.clear();
    });

    describe('POST /tickets', () => {
        it('should create a new ticket with valid data', async () => {
            const response = await request(app)
                .post('/tickets')
                .send({
                    customer_id: 'cust-001',
                    customer_email: 'test@example.com',
                    customer_name: 'Test User',
                    subject: 'Test ticket',
                    description: 'This is a test ticket description that is long enough to pass validation.',
                    tags: ['test'],
                    metadata: {
                        source: 'web_form',
                        browser: 'Chrome',
                        device_type: 'desktop'
                    }
                });

            expect(response.status).toBe(201);
            expect(response.body).toHaveProperty('id');
            expect(response.body.customer_email).toBe('test@example.com');
            expect(response.body.status).toBe('new');
            expect(response.body).toHaveProperty('classification');
            expect(response.body.classification).toHaveProperty('category');
            expect(response.body.classification).toHaveProperty('priority');
        });

        it('should reject ticket with invalid email', async () => {
            const response = await request(app)
                .post('/tickets')
                .send({
                    customer_id: 'cust-001',
                    customer_email: 'invalid-email',
                    customer_name: 'Test User',
                    subject: 'Test ticket',
                    description: 'This is a test ticket description that is long enough.',
                    tags: ['test'],
                    metadata: {
                        source: 'web_form',
                        browser: 'Chrome',
                        device_type: 'desktop'
                    }
                });

            expect(response.status).toBe(400);
            expect(response.body).toHaveProperty('error');
        });

        it('should reject ticket with missing required fields', async () => {
            const response = await request(app)
                .post('/tickets')
                .send({
                    customer_email: 'test@example.com'
                });

            expect(response.status).toBe(400);
            expect(response.body).toHaveProperty('error');
        });
    });

    describe('GET /tickets', () => {
        beforeEach(async () => {
            await request(app).post('/tickets').send({
                customer_id: 'cust-001',
                customer_email: 'user1@example.com',
                customer_name: 'User One',
                subject: 'Login issue - urgent',
                description: 'Cannot access my account. This is critical and blocking work.',
                tags: ['login', 'urgent'],
                metadata: { source: 'web_form', browser: 'Chrome', device_type: 'desktop' }
            });

            await request(app).post('/tickets').send({
                customer_id: 'cust-002',
                customer_email: 'user2@example.com',
                customer_name: 'User Two',
                subject: 'Feature suggestion: dark mode',
                description: 'Would be nice to have a dark mode option for the interface.',
                tags: ['feature'],
                metadata: { source: 'api', browser: null, device_type: 'mobile' }
            });
        });

        it('should return all tickets', async () => {
            const response = await request(app).get('/tickets');

            expect(response.status).toBe(200);
            expect(response.body).toHaveProperty('data');
            expect(response.body.data).toHaveLength(2);
        });

        it('should filter tickets by priority', async () => {
            const response = await request(app).get('/tickets?priority=urgent');

            expect(response.status).toBe(200);
            expect(response.body.data.length).toBeGreaterThan(0);
            expect(response.body.data[0].priority).toBe('urgent');
        });

        it('should filter tickets by category', async () => {
            const response = await request(app).get('/tickets?category=account_access');

            expect(response.status).toBe(200);
            if (response.body.data.length > 0) {
                expect(response.body.data[0].category).toBe('account_access');
            }
        });

        it('should paginate results with limit and offset', async () => {
            const response = await request(app).get('/tickets?limit=1&offset=0');

            expect(response.status).toBe(200);
            expect(response.body.data).toHaveLength(1);
        });
    });

    describe('GET /tickets/:id', () => {
        it('should return a specific ticket by ID', async () => {
            const createResponse = await request(app).post('/tickets').send({
                customer_id: 'cust-001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Test ticket',
                description: 'This is a test ticket with sufficient description length.',
                tags: ['test'],
                metadata: { source: 'web_form', browser: 'Chrome', device_type: 'desktop' }
            });

            const ticketId = createResponse.body.data.id;
            const response = await request(app).get(`/tickets/${ticketId}`);

            expect(response.status).toBe(200);
            expect(response.body.data.id).toBe(ticketId);
            expect(response.body.data.customer_email).toBe('test@example.com');
        });

        it('should return 404 for non-existent ticket', async () => {
            const response = await request(app).get('/tickets/00000000-0000-0000-0000-000000000000');

            expect(response.status).toBe(404);
            expect(response.body).toHaveProperty('error');
        });

        it('should return 400 for invalid UUID format', async () => {
            const response = await request(app).get('/tickets/invalid-uuid');

            expect(response.status).toBe(400);
            expect(response.body).toHaveProperty('error');
        });
    });

    describe('PUT /tickets/:id', () => {
        it('should update an existing ticket', async () => {
            const createResponse = await request(app).post('/tickets').send({
                customer_id: 'cust-001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Original subject',
                description: 'Original description that is long enough for validation.',
                tags: ['test'],
                metadata: { source: 'web_form', browser: 'Chrome', device_type: 'desktop' }
            });

            const ticketId = createResponse.body.data.id;
            const updateResponse = await request(app)
                .put(`/tickets/${ticketId}`)
                .send({
                    subject: 'Updated subject',
                    status: 'in_progress',
                    assigned_to: 'agent-john'
                });

            expect(updateResponse.status).toBe(200);
            expect(updateResponse.body.data.subject).toBe('Updated subject');
            expect(updateResponse.body.data.status).toBe('in_progress');
            expect(updateResponse.body.data.assigned_to).toBe('agent-john');
        });
    });

    describe('DELETE /tickets/:id', () => {
        it('should delete an existing ticket', async () => {
            const createResponse = await request(app).post('/tickets').send({
                customer_id: 'cust-001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'To be deleted',
                description: 'This ticket will be deleted in the test case.',
                tags: ['test'],
                metadata: { source: 'web_form', browser: 'Chrome', device_type: 'desktop' }
            });

            const ticketId = createResponse.body.data.id;
            const deleteResponse = await request(app).delete(`/tickets/${ticketId}`);

            expect(deleteResponse.status).toBe(200);
            expect(deleteResponse.body).toHaveProperty('message');

            const getResponse = await request(app).get(`/tickets/${ticketId}`);
            expect(getResponse.status).toBe(404);
        });
    });
});
