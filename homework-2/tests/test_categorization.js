const {
  autoClassify,
  classifyCategory,
  classifyPriority,
  CATEGORY_KEYWORDS,
  PRIORITY_KEYWORDS
} = require('../src/services/classifier');
const { resetStore } = require('../src/data/store');

describe('Auto-Classification', () => {
  beforeEach(() => {
    resetStore();
  });

  describe('classifyCategory', () => {
    test('classifies account_access issues', () => {
      const result = classifyCategory('Cannot login to my account, password reset not working');
      expect(result.category).toBe('account_access');
      expect(result.confidence).toBeGreaterThan(0);
      expect(result.keywords.length).toBeGreaterThan(0);
    });

    test('classifies technical_issue', () => {
      const result = classifyCategory('The application keeps crashing with an error message');
      expect(result.category).toBe('technical_issue');
    });

    test('classifies billing_question', () => {
      const result = classifyCategory('I need a refund for my payment. The invoice shows wrong amount.');
      expect(result.category).toBe('billing_question');
    });

    test('classifies feature_request', () => {
      const result = classifyCategory('It would be nice if you could add a new feature for export');
      expect(result.category).toBe('feature_request');
    });

    test('classifies bug_report', () => {
      const result = classifyCategory('Bug found: Steps to reproduce the defect - expected vs actual');
      expect(result.category).toBe('bug_report');
    });

    test('returns other for unclassifiable text', () => {
      const result = classifyCategory('Hello, I have a general question about your service.');
      expect(result.category).toBe('other');
      expect(result.confidence).toBe(0);
    });
  });

  describe('classifyPriority', () => {
    test('classifies urgent priority', () => {
      const result = classifyPriority("I can't access my account and this is critical for production");
      expect(result.priority).toBe('urgent');
      expect(result.confidence).toBe(0.9);
    });

    test('classifies high priority', () => {
      const result = classifyPriority('This is very important and blocking our work');
      expect(result.priority).toBe('high');
      expect(result.confidence).toBe(0.8);
    });

    test('classifies low priority', () => {
      const result = classifyPriority('Just a minor cosmetic suggestion for when you have time');
      expect(result.priority).toBe('low');
      expect(result.confidence).toBe(0.7);
    });

    test('defaults to medium priority', () => {
      const result = classifyPriority('I have a question about how to use this feature');
      expect(result.priority).toBe('medium');
      expect(result.confidence).toBe(0.5);
    });
  });

  describe('autoClassify', () => {
    test('classifies ticket and returns full result', () => {
      const ticket = {
        id: 'test-123',
        subject: 'Cannot login',
        description: 'I forgot my password and the reset email never arrives. This is urgent!'
      };

      const result = autoClassify(ticket);

      expect(result.ticket_id).toBe('test-123');
      expect(result.category).toBe('account_access');
      expect(result.priority).toBe('urgent');
      expect(result.reasoning).toBeDefined();
      expect(result.category_keywords.length).toBeGreaterThan(0);
    });

    test('handles production down as urgent', () => {
      const ticket = {
        id: 'test-456',
        subject: 'Production down',
        description: 'Our production environment is completely down and not working'
      };

      const result = autoClassify(ticket);
      expect(result.priority).toBe('urgent');
    });
  });
});
