import request from 'supertest';
import app from '../src/app';
import { ticketRepository } from '../src/repositories/ticket.repository';
import path from 'path';
import fs from 'fs';

describe('XML Import', () => {
    beforeEach(() => {
        ticketRepository.clear();
    });

    it('should successfully import valid XML file', async () => {
        const xmlPath = path.join(__dirname, 'fixtures', 'valid_tickets.xml');

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', xmlPath);

        expect(response.status).toBe(200);
        expect(response.body).toHaveProperty('imported');
        expect(response.body.imported).toBeGreaterThan(0);
        expect(response.body.tickets).toBeInstanceOf(Array);
    });

    it('should reject malformed XML file', async () => {
        const xmlPath = path.join(__dirname, 'fixtures', 'malformed.xml');

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', xmlPath);

        expect(response.status).toBe(400);
        expect(response.body).toHaveProperty('error');
    });

    it('should handle XML with invalid ticket data', async () => {
        const invalidXml = `<?xml version="1.0" encoding="UTF-8"?>
<tickets>
    <ticket>
        <customer_id>cust-300</customer_id>
        <customer_email>invalid-email</customer_email>
        <customer_name>Test User</customer_name>
        <subject>Invalid email</subject>
        <description>This ticket has an invalid email format.</description>        <category>technical</category>
        <priority>medium</priority>        <tags><tag>test</tag></tags>
        <metadata>
            <source>web_form</source>
            <browser>Chrome</browser>
            <device_type>desktop</device_type>
        </metadata>
    </ticket>
</tickets>`;

        const tmpPath = path.join(__dirname, 'fixtures', 'temp_invalid.xml');
        fs.writeFileSync(tmpPath, invalidXml);

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', tmpPath);

        fs.unlinkSync(tmpPath);

        expect(response.status).toBe(400);
        expect(response.body.failed).toBeGreaterThanOrEqual(1);
    });

    it('should handle empty XML tickets collection', async () => {
        const emptyXml = `<?xml version="1.0" encoding="UTF-8"?>
<tickets>
</tickets>`;

        const tmpPath = path.join(__dirname, 'fixtures', 'temp_empty.xml');
        fs.writeFileSync(tmpPath, emptyXml);

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', tmpPath);

        fs.unlinkSync(tmpPath);

        expect(response.status).toBe(400);
        expect(response.body.imported).toBe(0);
    });

    it('should parse XML with nested tags structure', async () => {
        const xmlWithNestedTags = `<?xml version="1.0" encoding="UTF-8"?>
<tickets>
    <ticket>
        <customer_id>cust-400</customer_id>
        <customer_email>nested@example.com</customer_email>
        <customer_name>Nested User</customer_name>
        <subject>Ticket with nested tags</subject>
        <description>This ticket description has sufficient length to pass validation rules.</description>
        <category>technical_issue</category>
        <priority>medium</priority>
        <tags>
            <tag>tag1</tag>
            <tag>tag2</tag>
            <tag>tag3</tag>
        </tags>
        <metadata>
            <source>api</source>
            <browser>Firefox</browser>
            <device_type>tablet</device_type>
        </metadata>
    </ticket>
</tickets>`;

        const tmpPath = path.join(__dirname, 'fixtures', 'temp_nested.xml');
        fs.writeFileSync(tmpPath, xmlWithNestedTags);

        const response = await request(app)
            .post('/tickets/import')
            .attach('file', tmpPath);

        fs.unlinkSync(tmpPath);

        expect(response.status).toBe(200);
        expect(response.body.imported).toBeGreaterThanOrEqual(1);
        if (response.body.tickets.length > 0) {
            expect(response.body.tickets[0].tags).toBeInstanceOf(Array);
        }
    });
});
