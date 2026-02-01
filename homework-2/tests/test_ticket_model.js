const { validateTicket } = require("../src/models/ticket");
const fixtures = require("./fixtures/tickets");

describe("Ticket Model Validation", () => {
  test("should validate a correct ticket", () => {
    const result = validateTicket(fixtures.validTicket);
    expect(result.valid).toBe(true);
    expect(result.data).toBeDefined();
  });

  test("should reject ticket with invalid email", () => {
    const result = validateTicket(fixtures.invalidTicketMissingEmail);
    expect(result.valid).toBe(false);
    expect(result.errors).toContainEqual(expect.objectContaining({
      field: "customer_email"
    }));
  });

  test("should reject ticket with description too short", () => {
    const result = validateTicket(fixtures.invalidTicketShortDescription);
    expect(result.valid).toBe(false);
    expect(result.errors).toContainEqual(expect.objectContaining({
      field: "description"
    }));
  });

  test("should reject ticket missing required field", () => {
    const result = validateTicket(fixtures.invalidTicketMissingRequired);
    expect(result.valid).toBe(false);
  });

  test("should apply default values", () => {
    const ticket = {
      customer_id: "TEST001",
      customer_email: "test@example.com",
      customer_name: "Test",
      subject: "Test Subject",
      description: "This is a long enough description"
    };
    const result = validateTicket(ticket);
    expect(result.valid).toBe(true);
    expect(result.data.category).toBe("other");
    expect(result.data.priority).toBe("medium");
    expect(result.data.status).toBe("new");
  });

  test("should validate subject length constraints", () => {
    const ticket = {
      ...fixtures.validTicket,
      subject: "a".repeat(201)
    };
    const result = validateTicket(ticket);
    expect(result.valid).toBe(false);
  });

  test("should validate description length constraints", () => {
    const ticket = {
      ...fixtures.validTicket,
      description: "a".repeat(2001)
    };
    const result = validateTicket(ticket);
    expect(result.valid).toBe(false);
  });

  test("should validate category enum", () => {
    const ticket = {
      ...fixtures.validTicket,
      category: "invalid_category"
    };
    const result = validateTicket(ticket);
    expect(result.valid).toBe(false);
  });

  test("should validate priority enum", () => {
    const ticket = {
      ...fixtures.validTicket,
      priority: "super_urgent"
    };
    const result = validateTicket(ticket);
    expect(result.valid).toBe(false);
  });
});
