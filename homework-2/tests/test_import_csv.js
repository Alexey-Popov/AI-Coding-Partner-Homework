const { importFromFile } = require("../src/services/importer");

describe("CSV Import", () => {
  test("should parse valid CSV data", () => {
    const csv = `customer_id,customer_email,customer_name,subject,description,category,priority
CUST001,john@example.com,John Doe,Login issue,Cannot login to my account,account_access,urgent
CUST002,jane@example.com,Jane Smith,Billing question,Wrong invoice amount,billing_question,high`;
    const buffer = Buffer.from(csv);
    const records = importFromFile(buffer, "csv");
    expect(records).toHaveLength(2);
    expect(records[0].customer_email).toBe("john@example.com");
  });

  test("should handle missing optional fields in CSV", () => {
    const csv = `customer_id,customer_email,customer_name,subject,description
CUST001,john@example.com,John Doe,Issue,Description`;
    const buffer = Buffer.from(csv);
    const records = importFromFile(buffer, "csv");
    expect(records[0].category).toBe("other");
    expect(records[0].priority).toBe("medium");
  });

  test("should parse CSV with default values gracefully", () => {
    const csv = `customer_id,customer_email,customer_name,subject,description
CUST001,john@example.com,John Doe,Test,Description here`;
    const buffer = Buffer.from(csv);
    const records = importFromFile(buffer, "csv");
    expect(records).toHaveLength(1);
    expect(records[0].category).toBe("other");
  });

  test("should parse tags from comma-separated CSV field", () => {
    const csv = `customer_id,customer_email,customer_name,subject,description,tags
CUST001,john@example.com,John Doe,Issue,This is a description,urgent billing`;
    const buffer = Buffer.from(csv);
    const records = importFromFile(buffer, "csv");
    expect(records[0].tags).toBeInstanceOf(Array);
  });

  test("should handle empty CSV", () => {
    const csv = "customer_id,customer_email,customer_name,subject,description";
    const buffer = Buffer.from(csv);
    const records = importFromFile(buffer, "csv");
    expect(records).toHaveLength(0);
  });

  test("should trim whitespace in CSV fields", () => {
    const csv = `customer_id,customer_email,customer_name,subject,description
CUST001, john@example.com , John Doe , Issue , Description `;
    const buffer = Buffer.from(csv);
    const records = importFromFile(buffer, "csv");
    expect(records[0].customer_email).toBe("john@example.com");
  });
});
