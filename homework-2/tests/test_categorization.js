const { autoClassify, extractKeywords } = require("../src/services/classifier");
const fixtures = require("./fixtures/tickets");

describe("Ticket Auto-Classification", () => {
  test("should classify account_access ticket correctly", () => {
    const ticket = {
      subject: "Cannot login",
      description: "I cannot access my account with my password"
    };
    const result = autoClassify(ticket);
    expect(result.category).toBe("account_access");
    expect(result.confidence).toBeGreaterThan(0);
  });

  test("should classify technical_issue correctly", () => {
    const ticket = {
      subject: "Application broken and not working",
      description: "The application is completely broken and failing to load properly"
    };
    const result = autoClassify(ticket);
    expect(result.category).toBe("technical_issue");
  });

  test("should classify billing_question correctly", () => {
    const ticket = {
      subject: "Invoice discrepancy",
      description: "I was charged twice on my invoice for the last month"
    };
    const result = autoClassify(ticket);
    expect(result.category).toBe("billing_question");
  });

  test("should classify feature_request correctly", () => {
    const ticket = {
      subject: "Dark mode feature",
      description: "I would like to request a dark mode feature for the app"
    };
    const result = autoClassify(ticket);
    expect(result.category).toBe("feature_request");
  });

  test("should assign urgent priority for critical keywords", () => {
    const ticket = {
      subject: "Production down security issue",
      description: "Critical system failure affecting production environment"
    };
    const result = autoClassify(ticket);
    expect(result.priority).toBe("urgent");
  });

  test("should assign high priority for important keywords", () => {
    const ticket = {
      subject: "Blocking issue preventing work",
      description: "This is an important problem that needs immediate attention asap"
    };
    const result = autoClassify(ticket);
    expect(result.priority).toBe("high");
  });

  test("should assign low priority for minor keywords", () => {
    const ticket = {
      subject: "Minor cosmetic issue",
      description: "This is a minor cosmetic suggestion for future improvement"
    };
    const result = autoClassify(ticket);
    expect(result.priority).toBe("low");
  });

  test("should extract keywords from text", () => {
    const text = "login password error crash";
    const keywords = extractKeywords(text);
    expect(keywords).toContain("login");
    expect(keywords).toContain("password");
    expect(keywords).toContain("error");
  });

  test("should return reasoning for classification", () => {
    const result = autoClassify(fixtures.ticketForClassification);
    expect(result.reasoning).toBeDefined();
    expect(result.reasoning.length).toBeGreaterThan(0);
  });

  test("should include keywords in response", () => {
    const result = autoClassify(fixtures.ticketForClassification);
    expect(result.keywords).toBeInstanceOf(Array);
  });
});
