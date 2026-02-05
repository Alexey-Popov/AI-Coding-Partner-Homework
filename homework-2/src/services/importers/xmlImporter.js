const xml2js = require('xml2js');

/**
 * Parse XML content into ticket objects
 */
async function parseXML(content) {
  try {
    const parser = new xml2js.Parser({
      explicitArray: false,
      trim: true
    });

    const result = await parser.parseStringPromise(content);

    // Support various XML structures
    let tickets = [];
    if (result.tickets && result.tickets.ticket) {
      tickets = Array.isArray(result.tickets.ticket)
        ? result.tickets.ticket
        : [result.tickets.ticket];
    } else if (result.ticket) {
      tickets = Array.isArray(result.ticket)
        ? result.ticket
        : [result.ticket];
    } else {
      throw new Error('XML must have a root "tickets" element with "ticket" children');
    }

    return tickets.map((record, index) => {
      // Handle tags
      let tags;
      if (record.tags) {
        if (typeof record.tags === 'string') {
          tags = record.tags.split(',').map(t => t.trim()).filter(t => t);
        } else if (record.tags.tag) {
          tags = Array.isArray(record.tags.tag) ? record.tags.tag : [record.tags.tag];
        }
      }

      // Handle metadata
      let metadata;
      if (record.metadata) {
        metadata = {
          source: record.metadata.source || undefined,
          browser: record.metadata.browser || undefined,
          device_type: record.metadata.device_type || undefined
        };
      }

      return {
        rowNumber: index + 1,
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
          tags: tags,
          metadata: metadata
        }
      };
    });
  } catch (error) {
    if (error.message.includes('XML')) {
      throw error;
    }
    throw new Error(`XML parsing error: ${error.message}`);
  }
}

module.exports = { parseXML };
