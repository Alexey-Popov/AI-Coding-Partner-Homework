import * as path from 'path';
import { XmlImportService } from '../src/services/XmlImportService';

describe('XmlImportService', () => {
  let service: XmlImportService;
  const fixturesPath = path.join(__dirname, 'fixtures');

  beforeEach(() => {
    service = new XmlImportService();
  });

  it('should import valid XML file', async () => {
    const filePath = path.join(fixturesPath, 'valid-tickets.xml');
    const result = await service.importFromFile(filePath);

    expect(result.success).toBe(true);
    expect(result.imported).toBe(3);
    expect(result.failed).toBe(0);
    expect(result.errors).toHaveLength(0);
    expect(result.validTickets).toHaveLength(3);
  });

  it('should parse XML with single ticket', async () => {
    const xmlData = `<?xml version="1.0"?>
<tickets>
  <ticket>
    <customer_id>cust_001</customer_id>
    <customer_email>test@example.com</customer_email>
    <customer_name>Test User</customer_name>
    <subject>Test subject</subject>
    <description>Test description for the ticket</description>
    <metadata>
      <source>web_form</source>
      <browser>Chrome</browser>
      <device_type>desktop</device_type>
    </metadata>
  </ticket>
</tickets>`;

    const result = await service.importFromString(xmlData);

    expect(result.success).toBe(true);
    expect(result.imported).toBe(1);
  });

  it('should parse XML with multiple tickets', async () => {
    const xmlData = `<?xml version="1.0"?>
<tickets>
  <ticket>
    <customer_id>cust_001</customer_id>
    <customer_email>test1@example.com</customer_email>
    <customer_name>User One</customer_name>
    <subject>First ticket</subject>
    <description>First ticket description</description>
    <metadata>
      <source>web_form</source>
      <browser>Chrome</browser>
      <device_type>desktop</device_type>
    </metadata>
  </ticket>
  <ticket>
    <customer_id>cust_002</customer_id>
    <customer_email>test2@example.com</customer_email>
    <customer_name>User Two</customer_name>
    <subject>Second ticket</subject>
    <description>Second ticket description</description>
    <metadata>
      <source>email</source>
      <browser>Firefox</browser>
      <device_type>mobile</device_type>
    </metadata>
  </ticket>
</tickets>`;

    const result = await service.importFromString(xmlData);

    expect(result.success).toBe(true);
    expect(result.imported).toBe(2);
  });

  it('should reject malformed XML', async () => {
    const xmlData = '<tickets><ticket><invalid</ticket>';
    const result = await service.importFromString(xmlData);

    expect(result.success).toBe(false);
    expect(result.errors[0]?.field).toBe('xml');
  });

  it('should reject XML without tickets root', async () => {
    const xmlData = `<?xml version="1.0"?>
<root>
  <item>test</item>
</root>`;

    const result = await service.importFromString(xmlData);

    expect(result.success).toBe(false);
    expect(result.errors[0]?.field).toBe('format');
  });

  it('should detect validation errors in XML tickets', async () => {
    const xmlData = `<?xml version="1.0"?>
<tickets>
  <ticket>
    <customer_id>cust_001</customer_id>
    <customer_email>invalid-email</customer_email>
    <customer_name>Test User</customer_name>
    <subject>Test</subject>
    <description>Description</description>
    <metadata>
      <source>web_form</source>
      <browser>Chrome</browser>
      <device_type>desktop</device_type>
    </metadata>
  </ticket>
</tickets>`;

    const result = await service.importFromString(xmlData);

    expect(result.success).toBe(false);
    expect(result.failed).toBe(1);
    expect(result.errors.length).toBeGreaterThan(0);
  });
});
