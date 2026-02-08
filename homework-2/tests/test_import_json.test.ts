import request from 'supertest';
import app from '../src/app';
import { ticketRepository } from '../src/repositories/ticket.repository';
import path from 'path';
import fs from 'fs';

describe('JSON Import', () => {
    beforeEach(() => {
        ticketRepository.clear();
    });

    it('should successfully import valid JSON file', async () => {
        const jsonPath = path.join(__dirname, 'fixtures', 'valid_tickets.json');

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', jsonPath);

        expect(response.status).toBe(201);
        expect(response.body).toHaveProperty('summary');
        expect(response.body.summary.total).toBeGreaterThan(0);
        expect(response.body.summary.successful).toBeGreaterThan(0);
        expect(response.body.tickets).toBeInstanceOf(Array);
    });

    it('should handle JSON with invalid records and report failures', async () => {
        const jsonPath = path.join(__dirname, 'fixtures', 'invalid_tickets.json');

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', jsonPath);

        expect(response.status).toBe(201);
        expect(response.body.summary.failed).toBeGreaterThan(0);
        expect(response.body).toHaveProperty('errors');
    });

    it('should reject malformed JSON file', async () => {
        const malformedJson = '{"invalid": json content without closing brace';
        const tmpPath = path.join(__dirname, 'fixtures', 'temp_malformed.json');
        fs.writeFileSync(tmpPath, malformedJson);

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', tmpPath);

        fs.unlinkSync(tmpPath);

        expect(response.status).toBe(400);
        expect(response.body).toHaveProperty('error');
    });

    it('should handle JSON array with mixed valid and invalid tickets', async () => {
        const mixedData = [
            {
                customer_id: 'cust-200',
                customer_email: 'valid@example.com',
                customer_name: 'Valid User',
                subject: 'Valid ticket',
                description: 'This is a valid ticket description with enough characters.',
                tags: ['test'],
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            },
            {
                customer_id: 'cust-201',
                customer_email: 'invalid-email',
                customer_name: 'Invalid User',
                subject: 'Invalid ticket',
                description: 'This ticket has an invalid email address.',
                tags: ['test'],
                metadata: {
                    source: 'api',
                    browser: null,
                    device_type: 'mobile'
                }
            }
        ];

        const tmpPath = path.join(__dirname, 'fixtures', 'temp_mixed.json');
        fs.writeFileSync(tmpPath, JSON.stringify(mixedData));

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', tmpPath);

        fs.unlinkSync(tmpPath);

        expect(response.status).toBe(201);
        expect(response.body.summary.successful).toBeGreaterThanOrEqual(1);
        expect(response.body.summary.failed).toBeGreaterThanOrEqual(1);
    });

    it('should handle empty JSON array', async () => {
        const emptyArray = '[]';
        const tmpPath = path.join(__dirname, 'fixtures', 'temp_empty.json');
        fs.writeFileSync(tmpPath, emptyArray);

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', tmpPath);

        fs.unlinkSync(tmpPath);

        expect(response.status).toBe(201);
        expect(response.body.summary.total).toBe(0);
        expect(response.body.summary.successful).toBe(0);
    });
});
