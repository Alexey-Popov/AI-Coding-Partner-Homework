import { CreateTicketSchemaType, UpdateTicketSchemaType, CreateTicketSchema, UpdateTicketSchema } from '../src/models/TicketValidator';

describe('Ticket Model Validation', () => {
    describe('CreateTicketInput Validation', () => {
        it('should validate a complete valid ticket', () => {
            const validTicket: CreateTicketSchemaType = {
                customer_id: 'cust-001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Valid ticket subject',
                description: 'This is a valid description that meets the minimum length requirement.',
                category: 'technical_issue',
                priority: 'medium',
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            };

            const result = CreateTicketSchema.safeParse(validTicket);
            expect(result.success).toBe(true);
        });

        it('should reject ticket with invalid email format', () => {
            const invalidTicket = {
                customer_id: 'cust-001',
                customer_email: 'not-an-email',
                customer_name: 'Test User',
                subject: 'Test subject',
                description: 'Valid description with sufficient length for validation rules.',
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            };

            const result = CreateTicketSchema.safeParse(invalidTicket);
            expect(result.success).toBe(false);
            if (!result.success) {
                expect(result.error.issues.some(issue => issue.path.includes('customer_email'))).toBe(true);
            }
        });

        it('should reject ticket with missing required fields', () => {
            const incompleteTicket = {
                customer_email: 'test@example.com',
                subject: 'Missing fields'
            };

            const result = CreateTicketSchema.safeParse(incompleteTicket);
            expect(result.success).toBe(false);
            if (!result.success) {
                expect(result.error.issues.length).toBeGreaterThan(0);
            }
        });

        it('should reject subject longer than 200 characters', () => {
            const longSubject = 'A'.repeat(201);
            const invalidTicket = {
                customer_id: 'cust-001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: longSubject,
                description: 'Valid description with enough length to pass validation.',
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            };

            const result = CreateTicketSchema.safeParse(invalidTicket);
            expect(result.success).toBe(false);
        });

        it('should reject description shorter than 10 characters', () => {
            const invalidTicket = {
                customer_id: 'cust-001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Test subject',
                description: 'Short',
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            };

            const result = CreateTicketSchema.safeParse(invalidTicket);
            expect(result.success).toBe(false);
        });

        it('should reject description longer than 2000 characters', () => {
            const longDescription = 'A'.repeat(2001);
            const invalidTicket = {
                customer_id: 'cust-001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Test subject',
                description: longDescription,
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            };

            const result = CreateTicketSchema.safeParse(invalidTicket);
            expect(result.success).toBe(false);
        });

        it('should reject invalid metadata source value', () => {
            const invalidTicket = {
                customer_id: 'cust-001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Test subject',
                description: 'Valid description with sufficient length for validation.',
                metadata: {
                    source: 'invalid_source',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            };

            const result = CreateTicketSchema.safeParse(invalidTicket);
            expect(result.success).toBe(false);
        });

        it('should reject invalid device_type value', () => {
            const invalidTicket = {
                customer_id: 'cust-001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Test subject',
                description: 'Valid description with sufficient length for validation.',
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'smartwatch'
                }
            };

            const result = CreateTicketSchema.safeParse(invalidTicket);
            expect(result.success).toBe(false);
        });
    });

    describe('UpdateTicketInput Validation', () => {
        it('should validate partial update with only status', () => {
            const partialUpdate: UpdateTicketSchemaType = {
                status: 'in_progress'
            };

            const result = UpdateTicketSchema.safeParse(partialUpdate);
            expect(result.success).toBe(true);
        });
    });
});
