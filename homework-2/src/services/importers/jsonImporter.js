/**
 * Parse JSON content into ticket objects
 */
function parseJSON(content) {
  try {
    const data = JSON.parse(content);

    // Support both array and object with tickets property
    let tickets = Array.isArray(data) ? data : (data.tickets || []);

    if (!Array.isArray(tickets)) {
      throw new Error('JSON must be an array or an object with a "tickets" array property');
    }

    return tickets.map((record, index) => ({
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
        tags: Array.isArray(record.tags) ? record.tags : undefined,
        metadata: record.metadata || undefined
      }
    }));
  } catch (error) {
    if (error instanceof SyntaxError) {
      throw new Error(`JSON parsing error: Invalid JSON format - ${error.message}`);
    }
    throw new Error(`JSON parsing error: ${error.message}`);
  }
}

module.exports = { parseJSON };
