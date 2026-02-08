import { ClassificationService } from '../src/services/ClassificationService';

describe('Ticket Classification Service', () => {
    const classificationService = new ClassificationService();

    describe('Category Classification', () => {
        it('should classify login issues as account_access', () => {
            const subject = 'Cannot login to my account';
            const description = 'I am unable to log in. The password reset is not working either.';

            const result = classificationService.classify(subject, description);
            expect(result.category).toBe('account_access');
            expect(result.confidence).toBeGreaterThanOrEqual(0.5);
        });

        it('should classify 2FA issues as account_access', () => {
            const subject = '2FA code not arriving';
            const description = 'Two-factor authentication code is not being sent to my phone.';

            const result = classificationService.classify(subject, description);
            expect(result.category).toBe('account_access');
        });

        it('should classify payment issues as billing_question', () => {
            const subject = 'Double charged on my invoice';
            const description = 'I was charged twice for my subscription. Need a refund immediately.';

            const result = classificationService.classify(subject, description);
            expect(result.category).toBe('billing_question');
        });

        it('should classify crash reports as technical_issue', () => {
            const subject = 'Application crashes on startup';
            const description = 'The app crashes immediately when I try to open it. Error code 500 displayed.';

            const result = classificationService.classify(subject, description);
            expect(result.category).toBe('technical_issue');
        });

        it('should classify enhancement suggestions as feature_request', () => {
            const subject = 'Add dark mode theme';
            const description = 'It would be great to have a dark mode option for better night viewing.';

            const result = classificationService.classify(subject, description);
            expect(result.category).toBe('feature_request');
        });

        it('should classify bug reports with reproduction steps as bug_report', () => {
            const subject = 'Button not clickable';
            const description = 'Bug found: Submit button becomes unresponsive. Steps to reproduce: 1. Open form 2. Fill fields 3. Click submit.';

            const result = classificationService.classify(subject, description);
            expect(result.category).toBe('bug_report');
        });

        it('should classify ambiguous tickets as other', () => {
            const subject = 'General inquiry';
            const description = 'I have a question about your service availability in my region.';

            const result = classificationService.classify(subject, description);
            expect(result.category).toBe('other');
        });
    });

    describe('Priority Classification', () => {
        it('should assign urgent priority for critical keywords', () => {
            const subject = 'Critical production outage';
            const description = 'Our production system is completely down. Cannot access anything urgently!';

            const result = classificationService.classify(subject, description);
            expect(result.priority).toBe('urgent');
        });

        it('should assign high priority for important keywords', () => {
            const subject = 'Important data export issue';
            const description = 'Need to export data ASAP for compliance. This is blocking our audit.';

            const result = classificationService.classify(subject, description);
            expect(result.priority).toBe('high');
        });

        it('should assign low priority for minor issues', () => {
            const subject = 'Minor cosmetic issue';
            const description = 'Small suggestion: button alignment is slightly off on mobile view.';

            const result = classificationService.classify(subject, description);
            expect(result.priority).toBe('low');
        });
    });

    describe('Classification Confidence', () => {
        it('should return high confidence for clear categorization', () => {
            const subject = 'Password reset not working';
            const description = 'Cannot reset my password. The reset link does not work.';

            const result = classificationService.classify(subject, description);
            expect(result.confidence).toBeGreaterThanOrEqual(0);
            expect(result.confidence).toBeLessThanOrEqual(1);
        });
    });
});
