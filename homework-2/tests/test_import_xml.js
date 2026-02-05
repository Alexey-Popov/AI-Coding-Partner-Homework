const { parseXML } = require('../src/services/importers/xmlImporter');
const { importTickets } = require('../src/services/importers');
const { resetStore } = require('../src/data/store');
const fs = require('fs');
const path = require('path');

describe('XML Import', () => {
  beforeEach(() => {
    resetStore();
  });

  describe('parseXML', () => {
    test('parses valid XML content', async () => {
      const xml = `<?xml version="1.0"?>
<tickets>
  <ticket>
    <customer_id>CUST-001</customer_id>
    <customer_email>test@email.com</customer_email>
    <customer_name>John Doe</customer_name>
    <subject>Test Subject</subject>
    <description>Valid description here</description>
  </ticket>
</tickets>`;

      const records = await parseXML(xml);
      expect(records.length).toBe(1);
      expect(records[0].data.customer_id).toBe('CUST-001');
    });

    test('handles tags as nested elements', async () => {
      const xml = `<?xml version="1.0"?>
<tickets>
  <ticket>
    <customer_id>CUST-001</customer_id>
    <customer_email>test@email.com</customer_email>
    <customer_name>John Doe</customer_name>
    <subject>Test</subject>
    <description>Valid description</description>
    <tags><tag>tag1</tag><tag>tag2</tag></tags>
  </ticket>
</tickets>`;

      const records = await parseXML(xml);
      expect(records[0].data.tags).toEqual(['tag1', 'tag2']);
    });

    test('handles metadata nested element', async () => {
      const xml = `<?xml version="1.0"?>
<tickets>
  <ticket>
    <customer_id>CUST-001</customer_id>
    <customer_email>test@email.com</customer_email>
    <customer_name>John Doe</customer_name>
    <subject>Test</subject>
    <description>Valid description</description>
    <metadata>
      <source>web_form</source>
      <device_type>desktop</device_type>
    </metadata>
  </ticket>
</tickets>`;

      const records = await parseXML(xml);
      expect(records[0].data.metadata.source).toBe('web_form');
    });

    test('throws error for malformed XML', async () => {
      await expect(parseXML('<invalid xml')).rejects.toThrow();
    });
  });

  describe('importTickets XML', () => {
    test('imports valid XML file successfully', async () => {
      const xmlContent = fs.readFileSync(
        path.join(__dirname, '../data/sample_tickets.xml'),
        'utf-8'
      );

      const result = await importTickets(xmlContent, 'xml');

      expect(result.total).toBe(30);
      expect(result.successful).toBe(30);
      expect(result.failed).toBe(0);
    });
  });
});
