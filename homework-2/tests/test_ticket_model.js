const {
  validateTicket,
  createTicket,
  updateTicket,
  isValidEmail,
  CATEGORIES,
  PRIORITIES,
  STATUSES,
  SOURCES,
  DEVICE_TYPES
} = require('../src/models/ticket');

describe('Ticket Model', () => {
  describe('isValidEmail', () => {
    test('accepts valid email addresses', () => {
      expect(isValidEmail('test@example.com')).toBe(true);
      expect(isValidEmail('user.name@domain.org')).toBe(true);
      expect(isValidEmail('user+tag@email.co.uk')).toBe(true);
    });

    test('rejects invalid email addresses', () => {
      expect(isValidEmail('invalid')).toBe(false);
      expect(isValidEmail('missing@domain')).toBe(false);
      expect(isValidEmail('@nodomain.com')).toBe(false);
      expect(isValidEmail('')).toBe(false);
    });
  });

  describe('validateTicket - Create Mode', () => {
    const validTicket = {
      customer_id: 'CUST-001',
      customer_email: 'test@email.com',
      customer_name: 'John Doe',
      subject: 'Test Subject',
      description: 'This is a valid description with enough characters.'
    };

    test('returns empty array for valid ticket', () => {
      const errors = validateTicket(validTicket);
      expect(errors).toEqual([]);
    });

    test('returns error for missing required fields', () => {
      const errors = validateTicket({});
      expect(errors.length).toBe(5);
      expect(errors.map(e => e.field)).toContain('customer_id');
      expect(errors.map(e => e.field)).toContain('customer_email');
      expect(errors.map(e => e.field)).toContain('customer_name');
      expect(errors.map(e => e.field)).toContain('subject');
      expect(errors.map(e => e.field)).toContain('description');
    });

    test('returns error for invalid email format', () => {
      const errors = validateTicket({ ...validTicket, customer_email: 'invalid' });
      expect(errors).toContainEqual({
        field: 'customer_email',
        message: 'customer_email must be a valid email address'
      });
    });

    test('returns error for subject too long', () => {
      const errors = validateTicket({ ...validTicket, subject: 'x'.repeat(201) });
      expect(errors).toContainEqual({
        field: 'subject',
        message: 'subject must be 1-200 characters'
      });
    });

    test('returns error for description too short', () => {
      const errors = validateTicket({ ...validTicket, description: 'short' });
      expect(errors).toContainEqual({
        field: 'description',
        message: 'description must be 10-2000 characters'
      });
    });

    test('returns error for invalid category', () => {
      const errors = validateTicket({ ...validTicket, category: 'invalid' });
      expect(errors.some(e => e.field === 'category')).toBe(true);
    });

    test('returns error for invalid priority', () => {
      const errors = validateTicket({ ...validTicket, priority: 'super_urgent' });
      expect(errors.some(e => e.field === 'priority')).toBe(true);
    });
  });

  describe('createTicket', () => {
    test('creates ticket with all required fields', () => {
      const data = {
        customer_id: 'CUST-001',
        customer_email: 'test@email.com',
        customer_name: 'John Doe',
        subject: 'Test Subject',
        description: 'Test description with enough length'
      };
      const ticket = createTicket(data);

      expect(ticket.id).toBeDefined();
      expect(ticket.customer_id).toBe('CUST-001');
      expect(ticket.status).toBe('new');
      expect(ticket.priority).toBe('medium');
      expect(ticket.category).toBe('other');
      expect(ticket.created_at).toBeDefined();
      expect(ticket.resolved_at).toBeNull();
    });
  });

  describe('Enum Constants', () => {
    test('CATEGORIES contains all expected values', () => {
      expect(CATEGORIES).toContain('account_access');
      expect(CATEGORIES).toContain('technical_issue');
      expect(CATEGORIES).toContain('billing_question');
      expect(CATEGORIES).toContain('feature_request');
      expect(CATEGORIES).toContain('bug_report');
      expect(CATEGORIES).toContain('other');
    });
  });
});
