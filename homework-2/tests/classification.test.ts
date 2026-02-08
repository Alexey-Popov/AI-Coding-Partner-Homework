import { ClassificationService } from '../src/services/ClassificationService';

describe('ClassificationService', () => {
  let service: ClassificationService;

  beforeEach(() => {
    service = new ClassificationService();
  });

  describe('Category Classification', () => {
    it('should classify as account_access', () => {
      const result = service.classify('Login issue', "I can't sign in with my password");
      expect(result.category).toBe('account_access');
      expect(result.keywords).toContain('login');
      expect(result.confidence).toBeGreaterThan(0);
    });

    it('should classify as technical_issue', () => {
      const result = service.classify('Error 500', 'App crash when clicking submit button');
      expect(result.category).toBe('technical_issue');
      expect(result.keywords).toContain('error');
      expect(result.keywords).toContain('crash');
    });

    it('should classify as billing_question', () => {
      const result = service.classify('Payment failed', 'Need refund for duplicate charge on invoice');
      expect(result.category).toBe('billing_question');
      expect(result.keywords).toContain('payment');
      expect(result.keywords).toContain('refund');
    });

    it('should classify as feature_request', () => {
      const result = service.classify('Enhancement suggestion', 'Would like to add dark mode');
      expect(result.category).toBe('feature_request');
      expect(result.keywords).toContain('suggestion');
      expect(result.keywords).toContain('add');
    });

    it('should classify as bug_report', () => {
      const result = service.classify('Bug found', 'Steps to reproduce: expected result differs from actual');
      expect(result.category).toBe('bug_report');
      expect(result.keywords).toContain('reproduce');
      expect(result.keywords).toContain('expected');
    });

    it('should classify as other when no keywords match', () => {
      const result = service.classify('Random topic', 'Some unrelated content here');
      expect(result.category).toBe('other');
      expect(result.reasoning).toContain('default');
    });
  });

  describe('Priority Classification', () => {
    it('should classify as urgent priority', () => {
      const result = service.classify('Critical issue', "Can't access production system");
      expect(result.priority).toBe('urgent');
      expect(result.keywords).toContain("can't access");
    });

    it('should classify as high priority', () => {
      const result = service.classify('Blocking issue', 'This is urgent and important');
      expect(result.priority).toBe('high');
      expect(result.keywords).toContain('important');
    });

    it('should classify as low priority', () => {
      const result = service.classify('Minor typo', 'Cosmetic suggestion for UI');
      expect(result.priority).toBe('low');
      expect(result.keywords).toContain('minor');
    });

    it('should classify as medium priority by default', () => {
      const result = service.classify('Normal request', 'Regular support question');
      expect(result.priority).toBe('medium');
      expect(result.reasoning).toContain('default');
    });
  });
});
