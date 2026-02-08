import { ClassificationService } from '../src/services/ClassificationService';
import { CreateTicketInput } from '../src/models/TicketValidator';

describe('Ticket Classification Service', () => {
    const classificationService = new ClassificationService();

    describe('Category Classification', () => {
        it('should classify login issues as account_access', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-001',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Cannot login to my account',
                description: 'I am unable to log in. The password reset is not working either.',
                tags: ['login'],
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.category).toBe('account_access');
            expect(result.confidence).toBeGreaterThan(0.5);
        });

        it('should classify 2FA issues as account_access', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-002',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: '2FA code not arriving',
                description: 'Two-factor authentication code is not being sent to my phone.',
                tags: ['2fa', 'authentication'],
                metadata: {
                    source: 'email',
                    browser: null,
                    device_type: 'mobile'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.category).toBe('account_access');
        });

        it('should classify payment issues as billing_question', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-003',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Double charged on my invoice',
                description: 'I was charged twice for my subscription. Need a refund immediately.',
                tags: ['billing', 'refund'],
                metadata: {
                    source: 'chat',
                    browser: 'Safari',
                    device_type: 'desktop'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.category).toBe('billing_question');
        });

        it('should classify crash reports as technical_issue', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-004',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Application crashes on startup',
                description: 'The app crashes immediately when I try to open it. Error code 500 displayed.',
                tags: ['crash', 'error'],
                metadata: {
                    source: 'api',
                    browser: null,
                    device_type: 'mobile'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.category).toBe('technical_issue');
        });

        it('should classify enhancement suggestions as feature_request', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-005',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Add dark mode theme',
                description: 'It would be great to have a dark mode option for better night viewing.',
                tags: ['feature', 'enhancement'],
                metadata: {
                    source: 'web_form',
                    browser: 'Firefox',
                    device_type: 'desktop'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.category).toBe('feature_request');
        });

        it('should classify bug reports with reproduction steps as bug_report', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-006',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Button not clickable',
                description: 'Bug found: Submit button becomes unresponsive. Steps to reproduce: 1. Open form 2. Fill fields 3. Click submit.',
                tags: ['bug', 'ui'],
                metadata: {
                    source: 'api',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.category).toBe('bug_report');
        });

        it('should classify ambiguous tickets as other', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-007',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'General inquiry',
                description: 'I have a question about your service availability in my region.',
                tags: ['question'],
                metadata: {
                    source: 'email',
                    browser: null,
                    device_type: 'mobile'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.category).toBe('other');
        });
    });

    describe('Priority Classification', () => {
        it('should assign urgent priority for critical keywords', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-008',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Critical production outage',
                description: 'Our production system is completely down. Cannot access anything urgently!',
                tags: ['critical'],
                metadata: {
                    source: 'chat',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.priority).toBe('urgent');
        });

        it('should assign high priority for important keywords', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-009',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Important data export issue',
                description: 'Need to export data ASAP for compliance. This is blocking our audit.',
                tags: ['important', 'blocking'],
                metadata: {
                    source: 'email',
                    browser: null,
                    device_type: 'desktop'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.priority).toBe('high');
        });

        it('should assign low priority for minor issues', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-010',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Minor cosmetic issue',
                description: 'Small suggestion: button alignment is slightly off on mobile view.',
                tags: ['cosmetic', 'minor'],
                metadata: {
                    source: 'web_form',
                    browser: 'Safari',
                    device_type: 'mobile'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.priority).toBe('low');
        });
    });

    describe('Classification Confidence', () => {
        it('should return high confidence for clear categorization', () => {
            const ticket: CreateTicketInput = {
                customer_id: 'cust-011',
                customer_email: 'test@example.com',
                customer_name: 'Test User',
                subject: 'Password reset not working',
                description: 'Cannot reset my password. The reset link does not work.',
                tags: ['password', 'login', 'authentication'],
                metadata: {
                    source: 'web_form',
                    browser: 'Chrome',
                    device_type: 'desktop'
                }
            };

            const result = classificationService.classify(ticket);
            expect(result.confidence).toBeGreaterThan(0.5);
            expect(result.confidence).toBeLessThanOrEqual(1);
        });
    });
});
