import { safeValidateCreateTicket } from '../src/models/TicketValidator';

describe('Ticket Model Validation', () => {
    const validTicketData = {
        customer_id: 'cust_123',
        customer_email: 'test@example.com',
        customer_name: 'John Doe',
        subject: 'Login issue',
        description: 'Cannot access my account after password reset',
        category: 'account_access' as const,
        priority: 'high' as const,
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
            // tags and assigned_to are system-generated, not part of CreateTicketSchema
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

    it('should ignore system-generated fields if provided by user', () => {
        const dataWithSystemFields = { 
            ...validTicketData, 
            assigned_to: 'agent-123',
            tags: ['user-tag']
        };
        const result = safeValidateCreateTicket(dataWithSystemFields);
        // Should succeed but system fields will be stripped by schema
        expect(result.success).toBe(true);
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
