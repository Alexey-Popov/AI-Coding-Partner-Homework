import * as path from 'path';
import { CsvImportService } from '../src/services/CsvImportService';

describe('CsvImportService', () => {
    let service: CsvImportService;
    const fixturesPath = path.join(__dirname, 'fixtures');

    beforeEach(() => {
        service = new CsvImportService();
    });

    it('should import valid CSV successfully', async () => {
        const filePath = path.join(fixturesPath, 'valid_tickets.csv');
        const result = await service.importFromFile(filePath);

        expect(result.success).toBe(true);
        expect(result.imported).toBe(6);
        expect(result.failed).toBe(0);
        expect(result.errors).toHaveLength(0);
    });

    it('should handle malformed CSV with extra columns', async () => {
        const filePath = path.join(fixturesPath, 'malformed-tickets.csv');
        const result = await service.importFromFile(filePath);

        expect(result.imported).toBeGreaterThan(0);
    });

    it('should reject CSV with missing required fields', async () => {
        const filePath = path.join(fixturesPath, 'missing-fields.csv');
        const result = await service.importFromFile(filePath);

        expect(result.success).toBe(false);
        expect(result.errors.length).toBeGreaterThan(0);
        expect(result.errors[0]?.field).toBe('headers');
    });

    it('should detect invalid email in row 5', async () => {
        const filePath = path.join(fixturesPath, 'invalid-email.csv');
        const result = await service.importFromFile(filePath);

        expect(result.success).toBe(false);
        expect(result.failed).toBe(1);
        expect(result.imported).toBe(4);

        const emailError = result.errors.find((e) => e.row === 6);
        expect(emailError).toBeDefined();
        expect(emailError?.field).toBe('customer_email');
    });

    it('should handle empty CSV file', async () => {
        const filePath = path.join(fixturesPath, 'empty.csv');
        const result = await service.importFromFile(filePath);

        expect(result.imported).toBe(0);
        expect(result.failed).toBe(0);
    });

    it('should process large CSV file with 100 rows', async () => {
        const filePath = path.join(fixturesPath, 'large-tickets.csv');
        const result = await service.importFromFile(filePath);

        expect(result.success).toBe(true);
        expect(result.imported).toBe(100);
        expect(result.failed).toBe(0);
    });
});
