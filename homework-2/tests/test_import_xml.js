const { importFromFile } = require("../src/services/importer");

describe("XML Import", () => {
  test("should parse valid XML tickets", () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customer_id>CUST001</customer_id>
    <customer_email>john@example.com</customer_email>
    <customer_name>John Doe</customer_name>
    <subject>Login issue</subject>
    <description>Cannot login to my account</description>
    <category>account_access</category>
  </ticket>
  <ticket>
    <customer_id>CUST002</customer_id>
    <customer_email>jane@example.com</customer_email>
    <customer_name>Jane Smith</customer_name>
    <subject>Billing question</subject>
    <description>Wrong invoice amount</description>
    <category>billing_question</category>
  </ticket>
</tickets>`;
    const buffer = Buffer.from(xml);
    const records = importFromFile(buffer, "xml");
    expect(records).toHaveLength(2);
    expect(records[0].customer_email).toBe("john@example.com");
  });

  test("should handle missing optional XML fields", () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customer_id>CUST001</customer_id>
    <customer_email>john@example.com</customer_email>
    <customer_name>John Doe</customer_name>
    <subject>Issue</subject>
    <description>Description</description>
  </ticket>
</tickets>`;
    const buffer = Buffer.from(xml);
    const records = importFromFile(buffer, "xml");
    expect(records[0].category).toBe("other");
    expect(records[0].priority).toBe("medium");
  });

  test("should handle malformed XML gracefully", () => {
    const xml = "<tickets></tickets>";
    const buffer = Buffer.from(xml);
    const records = importFromFile(buffer, "xml");
    expect(Array.isArray(records)).toBe(true);
  });

  test("should handle XML with nested metadata", () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customer_id>CUST001</customer_id>
    <customer_email>john@example.com</customer_email>
    <customer_name>John Doe</customer_name>
    <subject>Issue</subject>
    <description>Description</description>
    <metadata>
      <source>email</source>
      <browser>Chrome</browser>
      <device_type>mobile</device_type>
    </metadata>
  </ticket>
</tickets>`;
    const buffer = Buffer.from(xml);
    const records = importFromFile(buffer, "xml");
    expect(records[0].metadata.source).toBe("email");
    expect(records[0].metadata.browser).toBe("Chrome");
  });

  test("should handle XML with tags", () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customer_id>CUST001</customer_id>
    <customer_email>john@example.com</customer_email>
    <customer_name>John Doe</customer_name>
    <subject>Issue</subject>
    <description>Description</description>
    <tags>
      <tag>urgent</tag>
      <tag>billing</tag>
    </tags>
  </ticket>
</tickets>`;
    const buffer = Buffer.from(xml);
    const records = importFromFile(buffer, "xml");
    expect(records[0].tags).toContain("urgent");
    expect(records[0].tags).toContain("billing");
  });
});
