const { importFromFile } = require("../src/services/importer");

describe("JSON Import", () => {
  test("should parse valid JSON array", () => {
    const json = JSON.stringify([
      {
        customer_id: "CUST001",
        customer_email: "john@example.com",
        customer_name: "John Doe",
        subject: "Login issue",
        description: "Cannot login to my account"
      },
      {
        customer_id: "CUST002",
        customer_email: "jane@example.com",
        customer_name: "Jane Smith",
        subject: "Billing question",
        description: "Wrong invoice amount"
      }
    ]);
    const buffer = Buffer.from(json);
    const records = importFromFile(buffer, "json");
    expect(records).toHaveLength(2);
    expect(records[0].customer_email).toBe("john@example.com");
  });

  test("should parse JSON object as single record", () => {
    const json = JSON.stringify({
      customer_id: "CUST001",
      customer_email: "john@example.com",
      customer_name: "John Doe",
      subject: "Login issue",
      description: "Cannot login to my account"
    });
    const buffer = Buffer.from(json);
    const records = importFromFile(buffer, "json");
    expect(records).toHaveLength(1);
    expect(records[0].customer_email).toBe("john@example.com");
  });

  test("should handle missing optional fields in JSON", () => {
    const json = JSON.stringify({
      customer_id: "CUST001",
      customer_email: "john@example.com",
      customer_name: "John Doe",
      subject: "Issue",
      description: "Description"
    });
    const buffer = Buffer.from(json);
    const records = importFromFile(buffer, "json");
    expect(records[0].category).toBe("other");
    expect(records[0].priority).toBe("medium");
  });

  test("should throw error on malformed JSON", () => {
    const buffer = Buffer.from("{invalid json");
    expect(() => importFromFile(buffer, "json")).toThrow();
  });

  test("should handle JSON with tags array", () => {
    const json = JSON.stringify({
      customer_id: "CUST001",
      customer_email: "john@example.com",
      customer_name: "John Doe",
      subject: "Issue",
      description: "Description",
      tags: ["urgent", "billing"]
    });
    const buffer = Buffer.from(json);
    const records = importFromFile(buffer, "json");
    expect(records[0].tags).toEqual(["urgent", "billing"]);
  });

  test("should handle JSON with metadata object", () => {
    const json = JSON.stringify({
      customer_id: "CUST001",
      customer_email: "john@example.com",
      customer_name: "John Doe",
      subject: "Issue",
      description: "Description",
      metadata: {
        source: "email",
        browser: "Chrome",
        device_type: "mobile"
      }
    });
    const buffer = Buffer.from(json);
    const records = importFromFile(buffer, "json");
    expect(records[0].metadata.source).toBe("email");
  });
});
