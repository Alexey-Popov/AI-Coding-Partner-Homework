const { v4: uuidv4 } = require("uuid");

const tickets = [];

function nowIso() {
  return new Date().toISOString();
}

function createTicket(payload) {
  const timestamp = nowIso();
  const ticket = {
    id: uuidv4(),
    created_at: timestamp,
    updated_at: timestamp,
    resolved_at: null,
    assigned_to: null,
    tags: payload.tags || [],
    metadata: payload.metadata || {
      source: "api",
      browser: "unknown",
      device_type: "desktop"
    },
    classification_log: [],
    ...payload
  };
  tickets.push(ticket);
  return ticket;
}

function getTicket(id) {
  return tickets.find((ticket) => ticket.id === id) || null;
}

function listTickets(filters = {}) {
  return tickets.filter((ticket) => {
    if (filters.status && ticket.status !== filters.status) return false;
    if (filters.category && ticket.category !== filters.category) return false;
    if (filters.priority && ticket.priority !== filters.priority) return false;
    if (filters.customer_id && ticket.customer_id !== filters.customer_id) return false;
    if (filters.assigned_to && ticket.assigned_to !== filters.assigned_to) return false;
    return true;
  });
}

function updateTicket(id, updates) {
  const ticket = getTicket(id);
  if (!ticket) return null;
  Object.assign(ticket, updates, { updated_at: nowIso() });
  if (updates.status === "resolved" && !ticket.resolved_at) {
    ticket.resolved_at = nowIso();
  }
  if (updates.status !== "resolved") {
    ticket.resolved_at = updates.resolved_at ?? ticket.resolved_at;
  }
  return ticket;
}

function deleteTicket(id) {
  const index = tickets.findIndex((ticket) => ticket.id === id);
  if (index === -1) return false;
  tickets.splice(index, 1);
  return true;
}

function logClassification(ticketId, decision) {
  const ticket = getTicket(ticketId);
  if (!ticket) return null;
  ticket.classification_log.push({
    timestamp: nowIso(),
    ...decision
  });
  return ticket;
}

function resetStore() {
  tickets.splice(0, tickets.length);
}

module.exports = {
  createTicket,
  getTicket,
  listTickets,
  updateTicket,
  deleteTicket,
  logClassification,
  resetStore,
  nowIso
};
