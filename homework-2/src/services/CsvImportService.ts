import * as fs from 'fs';
import { safeValidateCreateTicket, CreateTicketSchemaType } from '../models/TicketValidator';

export interface CsvImportResult {
    success: boolean;
    imported: number;
    failed: number;
    errors: Array<{ row: number; field: string; message: string }>;
    validTickets: CreateTicketSchemaType[];
}

export class CsvImportService {
    public async importFromFile(filePath: string): Promise<CsvImportResult> {
        const content = fs.readFileSync(filePath, 'utf-8');
        return this.importFromString(content);
    }

    public importFromString(csvContent: string): CsvImportResult {
        const result: CsvImportResult = {
            success: true,
            imported: 0,
            failed: 0,
            errors: [],
            validTickets: [],
        };

        const lines = csvContent.trim().split('\n');
        if (lines.length === 0) {
            return result;
        }

        const headers = lines[0]?.split(',').map((h) => h.trim()) || [];
        const requiredHeaders = [
            'customer_id',
            'customer_email',
            'customer_name',
            'subject',
            'description',
            'source',
            'browser',
            'device_type',
        ];

        const missingHeaders = requiredHeaders.filter((h) => !headers.includes(h));
        if (missingHeaders.length > 0) {
            result.success = false;
            result.errors.push({
                row: 0,
                field: 'headers',
                message: `Missing required headers: ${missingHeaders.join(', ')}`,
            });
            return result;
        }

        for (let i = 1; i < lines.length; i++) {
            const line = lines[i]?.trim();
            if (!line) continue;

            // Parse CSV line with proper handling of quoted fields
            const values = this.parseCSVLine(line);
            const row: Record<string, string> = {};

            headers.forEach((header, index) => {
                row[header] = values[index] || '';
            });

            // Parse tags from CSV format (comma-separated string)
            const tagsString = row['tags'] || '';
            const tags = tagsString ? tagsString.split(',').map(t => t.trim()).filter(t => t) : [];

            const ticketData = {
                customer_id: row['customer_id'],
                customer_email: row['customer_email'],
                customer_name: row['customer_name'],
                subject: row['subject'],
                description: row['description'],
                category: row['category'],
                priority: row['priority'],
                tags: tags.length > 0 ? tags : undefined,
                metadata: {
                    source: row['source'],
                    browser: row['browser'] || undefined,
                    device_type: row['device_type'],
                },
            };

            const validation = safeValidateCreateTicket(ticketData);

            if (!validation.success) {
                result.failed++;
                result.success = false;
                validation.error.issues.forEach((err) => {
                    result.errors.push({
                        row: i + 1,
                        field: err.path.join('.'),
                        message: err.message,
                    });
                });
            } else {
                result.imported++;
                result.validTickets.push(validation.data);
            }
        }

        return result;
    }

    /**
     * Parse a CSV line handling quoted fields properly
     * Handles commas inside quoted strings
     */
    private parseCSVLine(line: string): string[] {
        const result: string[] = [];
        let current = '';
        let insideQuotes = false;

        for (let i = 0; i < line.length; i++) {
            const char = line[i];
            
            if (char === '"') {
                // Toggle quote state
                insideQuotes = !insideQuotes;
            } else if (char === ',' && !insideQuotes) {
                // End of field
                result.push(current.trim());
                current = '';
            } else {
                current += char;
            }
        }
        
        // Push the last field
        result.push(current.trim());
        
        return result;
    }
}
