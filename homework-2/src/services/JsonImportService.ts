import * as fs from 'fs';
import { safeValidateCreateTicket, CreateTicketSchemaType } from '../models/TicketValidator';

export interface JsonImportResult {
    success: boolean;
    imported: number;
    failed: number;
    errors: Array<{ index: number; field: string; message: string }>;
    validTickets: CreateTicketSchemaType[];
}

export class JsonImportService {
    public async importFromFile(filePath: string): Promise<JsonImportResult> {
        const content = fs.readFileSync(filePath, 'utf-8');
        return this.importFromString(content);
    }

    public importFromString(jsonContent: string): JsonImportResult {
        const result: JsonImportResult = {
            success: true,
            imported: 0,
            failed: 0,
            errors: [],
            validTickets: [],
        };

        let tickets: unknown[];

        try {
            const parsed = JSON.parse(jsonContent);

            if (Array.isArray(parsed)) {
                tickets = parsed;
            } else if (parsed && typeof parsed === 'object' && Array.isArray(parsed.tickets)) {
                tickets = parsed.tickets;
            } else {
                result.success = false;
                result.errors.push({
                    index: 0,
                    field: 'format',
                    message: 'JSON must be an array of tickets or an object with a "tickets" array',
                });
                return result;
            }
        } catch (error) {
            result.success = false;
            result.errors.push({
                index: 0,
                field: 'json',
                message: error instanceof Error ? error.message : 'Invalid JSON format',
            });
            return result;
        }

        tickets.forEach((ticketData, index) => {
            const validation = safeValidateCreateTicket(ticketData);

            if (!validation.success) {
                result.failed++;
                result.success = false;
                validation.error.issues.forEach((err) => {
                    result.errors.push({
                        index: index + 1,
                        field: err.path.join('.'),
                        message: err.message,
                    });
                });
            } else {
                result.imported++;
                result.validTickets.push(validation.data);
            }
        });

        return result;
    }
}
