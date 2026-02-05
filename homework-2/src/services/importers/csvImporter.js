const { parse } = require('csv-parse/sync');

/**
 * Parse CSV content into ticket objects
 */
function parseCSV(content) {
  try {
    const records = parse(content, {
      columns: true,
      skip_empty_lines: true,
      trim: true,
      relax_column_count: true
    });

    return records.map((record, index) => {
      // Handle tags (comma-separated in CSV)
      let tags = [];
      if (record.tags) {
        tags = record.tags.split(',').map(t => t.trim()).filter(t => t);
      }

      // Handle metadata
      let metadata = {};
      if (record.source || record.browser || record.device_type) {
        metadata = {
          source: record.source || undefined,
          browser: record.browser || undefined,
          device_type: record.device_type || undefined
        };
      }
      if (record.metadata) {
        try {
          metadata = { ...metadata, ...JSON.parse(record.metadata) };
        } catch (e) {
          // Ignore JSON parse errors for metadata
        }
      }

      return {
        rowNumber: index + 2, // +2 for header row and 0-indexing
        data: {
          customer_id: record.customer_id,
          customer_email: record.customer_email,
          customer_name: record.customer_name,
          subject: record.subject,
          description: record.description,
          category: record.category || undefined,
          priority: record.priority || undefined,
          status: record.status || undefined,
          assigned_to: record.assigned_to || undefined,
          tags: tags.length > 0 ? tags : undefined,
          metadata: Object.keys(metadata).length > 0 ? metadata : undefined
        }
      };
    });
  } catch (error) {
    throw new Error(`CSV parsing error: ${error.message}`);
  }
}

module.exports = { parseCSV };
