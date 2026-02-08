import request from 'supertest';
import app from '../src/app';
import { ticketRepository } from '../src/repositories/ticket.repository';
import path from 'path';
import fs from 'fs';

describe('CSV Import', () => {
    beforeEach(() => {
        ticketRepository.clear();
    });

    it('should successfully import valid CSV file', async () => {
        const csvPath = path.join(__dirname, 'fixtures', 'valid_tickets.csv');

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', csvPath);

        expect(response.status).toBe(200);
        expect(response.body).toHaveProperty('imported');
        expect(response.body.imported).toBeGreaterThan(0);
        expect(response.body).toHaveProperty('failed');
        expect(response.body.failed).toBeGreaterThanOrEqual(0);
    });

    it('should handle CSV with invalid records and report failures', async () => {
        const csvPath = path.join(__dirname, 'fixtures', 'invalid_tickets.csv');

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', csvPath);

        expect(response.status).toBe(400);
        expect(response.body.failed).toBeGreaterThan(0);
        expect(response.body).toHaveProperty('errors');
        expect(response.body.errors.length).toBeGreaterThan(0);
    });

    it('should reject CSV import without file upload', async () => {
        const response = await request(app)
            .post('/tickets/import');

        expect(response.status).toBe(400);
        expect(response.body).toHaveProperty('error');
    });

    it('should parse CSV with empty optional fields', async () => {
        const csvContent = `customer_id,customer_email,customer_name,subject,description,tags,source,browser,device_type,category,priority
cust-100,test@example.com,Test User,Test subject,This is a valid test description with enough characters.,,web_form,,desktop,technical_issue,medium`;

        const tmpPath = path.join(__dirname, 'fixtures', 'temp_test.csv');
        fs.writeFileSync(tmpPath, csvContent);

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', tmpPath);

        fs.unlinkSync(tmpPath);

        expect(response.status).toBe(200);
        expect(response.body.imported).toBeGreaterThanOrEqual(1);
    });

    it('should validate email format in CSV records', async () => {
        const csvContent = `customer_id,customer_email,customer_name,subject,description,tags,source,browser,device_type,category,priority
cust-100,invalid-email,Test User,Test subject,This description has enough characters to pass validation.,test,web_form,Chrome,desktop,technical_issue,low`;

        const tmpPath = path.join(__dirname, 'fixtures', 'temp_invalid_email.csv');
        fs.writeFileSync(tmpPath, csvContent);

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', tmpPath);

        fs.unlinkSync(tmpPath);

        expect(response.status).toBe(400);
        expect(response.body.failed).toBeGreaterThanOrEqual(1);
    });

    it('should handle CSV with special characters and quotes', async () => {
        const csvContent = `customer_id,customer_email,customer_name,subject,description,tags,source,browser,device_type,category,priority
cust-100,test@example.com,"User, Test","Subject with ""quotes""","Description with special chars: !@#$%^&*() and enough length to validate.",test,web_form,Chrome,desktop,technical_issue,medium`;

        const tmpPath = path.join(__dirname, 'fixtures', 'temp_special_chars.csv');
        fs.writeFileSync(tmpPath, csvContent);

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', tmpPath);

        fs.unlinkSync(tmpPath);

        expect(response.status).toBe(200);
        expect(response.body.imported).toBeGreaterThanOrEqual(1);
    });
});
