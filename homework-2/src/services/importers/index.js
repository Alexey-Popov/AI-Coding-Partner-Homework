const { parseCSV } = require('./csvImporter');
const { parseJSON } = require('./jsonImporter');
const { parseXML } = require('./xmlImporter');
const { validateTicket, createTicket } = require('../../models/ticket');
const { addTicket } = require('../../data/store');
const { autoClassify } = require('../classifier');

/**
 * Import tickets from file content
 */
async function importTickets(content, format, options = {}) {
  const { autoClassifyTickets = false } = options;

  let parsedRecords;

  // Parse based on format
  switch (format.toLowerCase()) {
    case 'csv':
      parsedRecords = parseCSV(content);
      break;
    case 'json':
      parsedRecords = parseJSON(content);
      break;
    case 'xml':
      parsedRecords = await parseXML(content);
      break;
    default:
      throw new Error(`Unsupported format: ${format}. Supported formats: csv, json, xml`);
  }

  const results = {
    total: parsedRecords.length,
    successful: 0,
    failed: 0,
    tickets: [],
    errors: []
  };

  // Process each record
  for (const record of parsedRecords) {
    const validationErrors = validateTicket(record.data);

    if (validationErrors.length > 0) {
      results.failed++;
      results.errors.push({
        row: record.rowNumber,
        errors: validationErrors
      });
    } else {
      try {
        const ticket = createTicket(record.data);

        // Auto-classify if requested
        if (autoClassifyTickets) {
          const classification = autoClassify(ticket);
          ticket.category = classification.category;
          ticket.priority = classification.priority;
        }

        addTicket(ticket);
        results.successful++;
        results.tickets.push(ticket);
      } catch (error) {
        results.failed++;
        results.errors.push({
          row: record.rowNumber,
          errors: [{ field: 'general', message: error.message }]
        });
      }
    }
  }

  return results;
}

module.exports = {
  importTickets,
  parseCSV,
  parseJSON,
  parseXML
};
