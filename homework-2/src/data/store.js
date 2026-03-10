// In-memory storage for tickets
const tickets = new Map();

// Classification logs
const classificationLogs = [];

/**
 * Get all tickets
 */
function getAllTickets() {
  return Array.from(tickets.values());
}

/**
 * Get ticket by ID
 */
function getTicketById(id) {
  return tickets.get(id);
}

/**
 * Add a new ticket
 */
function addTicket(ticket) {
  tickets.set(ticket.id, ticket);
  return ticket;
}

/**
 * Update a ticket
 */
function updateTicketInStore(id, ticket) {
  if (!tickets.has(id)) {
    return null;
  }
  tickets.set(id, ticket);
  return ticket;
}

/**
 * Delete a ticket
 */
function deleteTicket(id) {
  if (!tickets.has(id)) {
    return false;
  }
  tickets.delete(id);
  return true;
}

/**
 * Get filtered tickets
 */
function getFilteredTickets(filters) {
  let result = getAllTickets();

  if (filters.category) {
    result = result.filter(t => t.category === filters.category);
  }

  if (filters.priority) {
    result = result.filter(t => t.priority === filters.priority);
  }

  if (filters.status) {
    result = result.filter(t => t.status === filters.status);
  }

  if (filters.assigned_to) {
    result = result.filter(t => t.assigned_to === filters.assigned_to);
  }

  if (filters.customer_id) {
    result = result.filter(t => t.customer_id === filters.customer_id);
  }

  if (filters.from) {
    const fromDate = new Date(filters.from);
    result = result.filter(t => new Date(t.created_at) >= fromDate);
  }

  if (filters.to) {
    const toDate = new Date(filters.to);
    toDate.setHours(23, 59, 59, 999);
    result = result.filter(t => new Date(t.created_at) <= toDate);
  }

  return result;
}

/**
 * Add classification log entry
 */
function addClassificationLog(entry) {
  classificationLogs.push({
    ...entry,
    timestamp: new Date().toISOString()
  });
}

/**
 * Get classification logs for a ticket
 */
function getClassificationLogs(ticketId) {
  return classificationLogs.filter(log => log.ticket_id === ticketId);
}

/**
 * Reset the store (for testing)
 */
function resetStore() {
  tickets.clear();
  classificationLogs.length = 0;
}

/**
 * Get ticket count
 */
function getTicketCount() {
  return tickets.size;
}

module.exports = {
  getAllTickets,
  getTicketById,
  addTicket,
  updateTicketInStore,
  deleteTicket,
  getFilteredTickets,
  addClassificationLog,
  getClassificationLogs,
  resetStore,
  getTicketCount
};
