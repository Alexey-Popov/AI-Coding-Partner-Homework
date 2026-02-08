import { safeValidateCreateTicket } from '../src/models/TicketValidator';

describe('Ticket Model Validation', () => {
    const validTicketData = {
        customer_id: 'cust_123',
        customer_email: 'test@example.com',
        customer_name: 'John Doe',
        subject: 'Login issue',
        description: 'Cannot access my account after password reset',
        metadata: {
            source: 'web_form' as const,
            browser: 'Chrome 120',
            device_type: 'desktop' as const,
        },
    };

    it('should validate a valid ticket', () => {
        const result = safeValidateCreateTicket(validTicketData);
        expect(result.success).toBe(true);
        if (result.success) {
            expect(result.data.customer_email).toBe('test@example.com');
            expect(result.data.tags).toEqual([]);
            expect(result.data.assigned_to).toBe(null);
        }
    });

    it('should reject invalid email format', () => {
        const invalidData = { ...validTicketData, customer_email: 'not-an-email' };
        const result = safeValidateCreateTicket(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
            expect(result.error.issues[0]?.path).toContain('customer_email');
        }
    });

    it('should reject subject too short', () => {
        const invalidData = { ...validTicketData, subject: '' };
        const result = safeValidateCreateTicket(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
            expect(result.error.issues[0]?.message).toContain('at least 1 character');
        }
    });

    it('should reject subject too long', () => {
        const invalidData = { ...validTicketData, subject: 'a'.repeat(201) };
        const result = safeValidateCreateTicket(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
            expect(result.error.issues[0]?.message).toContain('not exceed 200');
        }
    });

    it('should reject description too short', () => {
        const invalidData = { ...validTicketData, description: 'short' };
        const result = safeValidateCreateTicket(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
            expect(result.error.issues[0]?.message).toContain('at least 10 characters');
        }
    });

    it('should reject description too long', () => {
        const invalidData = { ...validTicketData, description: 'a'.repeat(2001) };
        const result = safeValidateCreateTicket(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
            expect(result.error.issues[0]?.message).toContain('not exceed 2000');
        }
    });

    it('should reject missing required fields', () => {
        const invalidData = { customer_email: 'test@example.com' };
        const result = safeValidateCreateTicket(invalidData);
        expect(result.success).toBe(false);
        if (!result.success) {
            expect(result.error.issues.length).toBeGreaterThan(1);
        }
    });

    it('should handle nullable fields correctly', () => {
        const dataWithNulls = { ...validTicketData, assigned_to: null };
        const result = safeValidateCreateTicket(dataWithNulls);
        expect(result.success).toBe(true);
        if (result.success) {
            expect(result.data.assigned_to).toBe(null);
        }
    });

    it('should validate metadata structure', () => {
        const invalidMetadata = {
            ...validTicketData,
            metadata: { source: 'invalid_source', browser: '', device_type: 'laptop' },
        };
        const result = safeValidateCreateTicket(invalidMetadata);
        expect(result.success).toBe(false);
        if (!result.success) {
            expect(result.error.issues.length).toBeGreaterThan(0);
        }
    });
});
