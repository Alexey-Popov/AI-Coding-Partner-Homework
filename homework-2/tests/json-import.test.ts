import * as path from 'path';
import { JsonImportService } from '../src/services/JsonImportService';

describe('JsonImportService', () => {
    let service: JsonImportService;
    const fixturesPath = path.join(__dirname, 'fixtures');

    beforeEach(() => {
        service = new JsonImportService();
    });

    it('should import valid JSON file', async () => {
        const filePath = path.join(fixturesPath, 'valid-tickets.json');
        const result = await service.importFromFile(filePath);

        expect(result.success).toBe(true);
        expect(result.imported).toBe(3);
        expect(result.failed).toBe(0);
        expect(result.errors).toHaveLength(0);
        expect(result.validTickets).toHaveLength(3);
    });

    it('should handle JSON array format', () => {
        const jsonData = JSON.stringify([
            {
                customer_id: 'cust_001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Test subject',
                description: 'Test description for the ticket',
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'desktop',
                },
            },
        ]);

        const result = service.importFromString(jsonData);

        expect(result.success).toBe(true);
        expect(result.imported).toBe(1);
    });

    it('should handle JSON object with tickets array', () => {
        const jsonData = JSON.stringify({
            tickets: [
                {
                    customer_id: 'cust_001',
                    customer_email: 'test@example.com',
                    customer_name: 'Test User',
                    subject: 'Test subject',
                    description: 'Test description for the ticket',
                    metadata: {
                        source: 'web_form',
                        browser: 'Chrome',
                        device_type: 'desktop',
                    },
                },
            ],
        });

        const result = service.importFromString(jsonData);

        expect(result.success).toBe(true);
        expect(result.imported).toBe(1);
    });

    it('should detect invalid email in JSON', async () => {
        const filePath = path.join(fixturesPath, 'invalid-email.json');
        const result = await service.importFromFile(filePath);

        expect(result.success).toBe(false);
        expect(result.imported).toBe(1);
        expect(result.failed).toBe(1);
        expect(result.errors.length).toBeGreaterThan(0);
        expect(result.errors[0]?.field).toBe('customer_email');
    });

    it('should reject malformed JSON', async () => {
        const filePath = path.join(fixturesPath, 'malformed.json');
        const result = await service.importFromFile(filePath);

        expect(result.success).toBe(false);
        expect(result.errors[0]?.field).toBe('json');
    });

    it('should reject invalid JSON format', () => {
        const jsonData = JSON.stringify({ invalid: 'format' });
        const result = service.importFromString(jsonData);

        expect(result.success).toBe(false);
        expect(result.errors[0]?.field).toBe('format');
    });
});
