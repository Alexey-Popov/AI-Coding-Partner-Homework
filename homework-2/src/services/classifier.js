const categoryKeywords = {
  account_access: ["login", "password", "2fa", "access", "sign in", "locked", "account"],
  technical_issue: ["bug", "error", "crash", "fail", "broken", "issue", "not working"],
  billing_question: ["invoice", "payment", "refund", "bill", "charge", "subscription", "cost"],
  feature_request: ["feature", "suggestion", "enhancement", "add", "request", "idea"],
  bug_report: ["bug", "defect", "reproduction", "steps", "error", "crash"],
  other: []
};

const priorityKeywords = {
  urgent: ["can't access", "critical", "production down", "security", "urgent"],
  high: ["important", "blocking", "asap", "high priority"],
  medium: ["medium", "soon"],
  low: ["minor", "cosmetic", "suggestion", "low priority"]
};

function extractKeywords(text) {
  if (!text) return [];
  const lower = text.toLowerCase();
  const found = [];
  const allKeywords = Object.values(categoryKeywords).flat().concat(
    Object.values(priorityKeywords).flat()
  );
  allKeywords.forEach((keyword) => {
    if (lower.includes(keyword)) {
      found.push(keyword);
    }
  });
  return [...new Set(found)];
}

function classifyCategory(subject, description) {
  const text = `${subject} ${description}`.toLowerCase();
  let bestCategory = "other";
  let bestScore = 0;

  Object.entries(categoryKeywords).forEach(([category, keywords]) => {
    if (keywords.length === 0) return;
    const matches = keywords.filter((kw) => text.includes(kw)).length;
    const score = matches / keywords.length;
    if (score > bestScore) {
      bestScore = score;
      bestCategory = category;
    }
  });

  return { category: bestCategory, confidence: Math.min(bestScore * 1.5, 1.0) };
}

function classifyPriority(subject, description) {
  const text = `${subject} ${description}`.toLowerCase();
  let bestPriority = "medium";
  let bestScore = 0;

  Object.entries(priorityKeywords).forEach(([priority, keywords]) => {
    if (keywords.length === 0) return;
    const matches = keywords.filter((kw) => text.includes(kw)).length;
    const score = matches / keywords.length;
    if (score > bestScore) {
      bestScore = score;
      bestPriority = priority;
    }
  });

  return { priority: bestPriority, confidence: Math.min(bestScore * 1.5, 1.0) };
}

function autoClassify(ticket) {
  const keywords = extractKeywords(`${ticket.subject} ${ticket.description}`);
  const categoryResult = classifyCategory(ticket.subject, ticket.description);
  const priorityResult = classifyPriority(ticket.subject, ticket.description);

  const reasoning = [];
  if (categoryResult.confidence > 0.5) {
    reasoning.push(`Category "${categoryResult.category}" matched with ${(categoryResult.confidence * 100).toFixed(1)}% confidence`);
  }
  if (priorityResult.confidence > 0.5) {
    reasoning.push(`Priority "${priorityResult.priority}" matched with ${(priorityResult.confidence * 100).toFixed(1)}% confidence`);
  }

  return {
    category: categoryResult.category,
    priority: priorityResult.priority,
    confidence: Math.round((categoryResult.confidence + priorityResult.confidence) / 2 * 100) / 100,
    reasoning: reasoning.length > 0 ? reasoning.join("; ") : "Default classification applied",
    keywords: keywords
  };
}

module.exports = {
  autoClassify,
  extractKeywords,
  classifyCategory,
  classifyPriority
};
