const { addClassificationLog } = require('../data/store');

// Category keywords mapping
const CATEGORY_KEYWORDS = {
  account_access: [
    'login', 'password', 'sign in', 'signin', 'log in', 'cant access', "can't access",
    'locked out', 'reset password', '2fa', 'two-factor', 'two factor', 'authentication',
    'mfa', 'multi-factor', 'account locked', 'forgot password', 'credentials',
    'username', 'access denied', 'permission denied'
  ],
  technical_issue: [
    'error', 'bug', 'crash', 'not working', 'broken', 'issue', 'problem',
    'failed', 'failure', 'exception', 'timeout', 'slow', 'freeze', 'freezing',
    'hang', 'hanging', 'unresponsive', 'loading', 'won\'t load', 'doesn\'t work',
    'stopped working', 'malfunction'
  ],
  billing_question: [
    'payment', 'invoice', 'charge', 'bill', 'billing', 'refund', 'subscription',
    'price', 'pricing', 'cost', 'fee', 'money', 'credit card', 'transaction',
    'receipt', 'overcharge', 'cancel subscription', 'renewal', 'upgrade', 'downgrade'
  ],
  feature_request: [
    'feature', 'request', 'suggestion', 'would be nice', 'could you add',
    'enhancement', 'improve', 'improvement', 'wish', 'want', 'need', 'missing',
    'add feature', 'new feature', 'integrate', 'integration', 'support for'
  ],
  bug_report: [
    'bug', 'defect', 'reproduce', 'steps to reproduce', 'expected', 'actual',
    'regression', 'broken', 'incorrect', 'wrong', 'should be', 'instead of',
    'not as expected', 'behavior', 'behaviour'
  ]
};

// Priority keywords mapping
const PRIORITY_KEYWORDS = {
  urgent: [
    "can't access", 'cannot access', 'critical', 'production down', 'security',
    'urgent', 'emergency', 'immediately', 'asap', 'right now', 'data loss',
    'security breach', 'hacked', 'compromised', 'outage', 'down'
  ],
  high: [
    'important', 'blocking', 'blocker', 'high priority', 'significant',
    'major', 'severe', 'impacting', 'business critical', 'deadline'
  ],
  low: [
    'minor', 'cosmetic', 'suggestion', 'when you have time', 'low priority',
    'nice to have', 'not urgent', 'eventually', 'someday', 'small'
  ]
};

/**
 * Classify ticket category based on subject and description
 */
function classifyCategory(text) {
  const normalizedText = text.toLowerCase();
  const scores = {};
  const foundKeywords = {};

  for (const [category, keywords] of Object.entries(CATEGORY_KEYWORDS)) {
    scores[category] = 0;
    foundKeywords[category] = [];

    for (const keyword of keywords) {
      if (normalizedText.includes(keyword.toLowerCase())) {
        scores[category]++;
        foundKeywords[category].push(keyword);
      }
    }
  }

  // Find best match
  let bestCategory = 'other';
  let bestScore = 0;
  let matchedKeywords = [];

  for (const [category, score] of Object.entries(scores)) {
    if (score > bestScore) {
      bestScore = score;
      bestCategory = category;
      matchedKeywords = foundKeywords[category];
    }
  }

  // Calculate confidence (0-1)
  const totalKeywordsChecked = Object.values(CATEGORY_KEYWORDS).flat().length;
  const maxPossibleScore = CATEGORY_KEYWORDS[bestCategory]?.length || 1;
  const confidence = bestScore > 0 ? Math.min(bestScore / Math.min(maxPossibleScore, 5), 1) : 0;

  return {
    category: bestCategory,
    confidence: Math.round(confidence * 100) / 100,
    keywords: matchedKeywords
  };
}

/**
 * Classify ticket priority based on subject and description
 */
function classifyPriority(text) {
  const normalizedText = text.toLowerCase();
  const foundKeywords = [];

  // Check urgent keywords first
  for (const keyword of PRIORITY_KEYWORDS.urgent) {
    if (normalizedText.includes(keyword.toLowerCase())) {
      foundKeywords.push(keyword);
      return { priority: 'urgent', confidence: 0.9, keywords: foundKeywords };
    }
  }

  // Check high priority keywords
  for (const keyword of PRIORITY_KEYWORDS.high) {
    if (normalizedText.includes(keyword.toLowerCase())) {
      foundKeywords.push(keyword);
      return { priority: 'high', confidence: 0.8, keywords: foundKeywords };
    }
  }

  // Check low priority keywords
  for (const keyword of PRIORITY_KEYWORDS.low) {
    if (normalizedText.includes(keyword.toLowerCase())) {
      foundKeywords.push(keyword);
      return { priority: 'low', confidence: 0.7, keywords: foundKeywords };
    }
  }

  // Default to medium
  return { priority: 'medium', confidence: 0.5, keywords: [] };
}

/**
 * Auto-classify a ticket
 */
function autoClassify(ticket) {
  const text = `${ticket.subject} ${ticket.description}`;

  const categoryResult = classifyCategory(text);
  const priorityResult = classifyPriority(text);

  const result = {
    ticket_id: ticket.id,
    category: categoryResult.category,
    category_confidence: categoryResult.confidence,
    category_keywords: categoryResult.keywords,
    priority: priorityResult.priority,
    priority_confidence: priorityResult.confidence,
    priority_keywords: priorityResult.keywords,
    reasoning: generateReasoning(categoryResult, priorityResult)
  };

  // Log the classification decision
  addClassificationLog({
    ticket_id: ticket.id,
    ...result
  });

  return result;
}

/**
 * Generate reasoning for classification
 */
function generateReasoning(categoryResult, priorityResult) {
  const parts = [];

  if (categoryResult.keywords.length > 0) {
    parts.push(`Category '${categoryResult.category}' detected based on keywords: ${categoryResult.keywords.join(', ')}`);
  } else {
    parts.push(`Category set to '${categoryResult.category}' (default) - no specific keywords matched`);
  }

  if (priorityResult.keywords.length > 0) {
    parts.push(`Priority '${priorityResult.priority}' assigned based on keywords: ${priorityResult.keywords.join(', ')}`);
  } else {
    parts.push(`Priority set to '${priorityResult.priority}' (default)`);
  }

  return parts.join('. ');
}

module.exports = {
  autoClassify,
  classifyCategory,
  classifyPriority,
  CATEGORY_KEYWORDS,
  PRIORITY_KEYWORDS
};
