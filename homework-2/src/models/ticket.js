const { v4: uuidv4 } = require('uuid');

// Valid enum values
const CATEGORIES = ['account_access', 'technical_issue', 'billing_question', 'feature_request', 'bug_report', 'other'];
const PRIORITIES = ['urgent', 'high', 'medium', 'low'];
const STATUSES = ['new', 'in_progress', 'waiting_customer', 'resolved', 'closed'];
const SOURCES = ['web_form', 'email', 'api', 'chat', 'phone'];
const DEVICE_TYPES = ['desktop', 'mobile', 'tablet'];

/**
 * Validates email format
 */
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Validates ticket data and returns array of errors
 */
function validateTicket(data, isUpdate = false) {
  const errors = [];

  // Required fields for create
  if (!isUpdate) {
    if (!data.customer_id) {
      errors.push({ field: 'customer_id', message: 'customer_id is required' });
    }

    if (!data.customer_email) {
      errors.push({ field: 'customer_email', message: 'customer_email is required' });
    } else if (!isValidEmail(data.customer_email)) {
      errors.push({ field: 'customer_email', message: 'customer_email must be a valid email address' });
    }

    if (!data.customer_name) {
      errors.push({ field: 'customer_name', message: 'customer_name is required' });
    }

    if (!data.subject) {
      errors.push({ field: 'subject', message: 'subject is required' });
    }

    if (!data.description) {
      errors.push({ field: 'description', message: 'description is required' });
    }
  }

  // Validate email format if provided
  if (data.customer_email && !isValidEmail(data.customer_email)) {
    if (isUpdate) {
      errors.push({ field: 'customer_email', message: 'customer_email must be a valid email address' });
    }
  }

  // Validate subject length
  if (data.subject !== undefined) {
    if (typeof data.subject !== 'string' || data.subject.length < 1 || data.subject.length > 200) {
      errors.push({ field: 'subject', message: 'subject must be 1-200 characters' });
    }
  }

  // Validate description length
  if (data.description !== undefined) {
    if (typeof data.description !== 'string' || data.description.length < 10 || data.description.length > 2000) {
      errors.push({ field: 'description', message: 'description must be 10-2000 characters' });
    }
  }

  // Validate category enum
  if (data.category !== undefined && !CATEGORIES.includes(data.category)) {
    errors.push({ field: 'category', message: `category must be one of: ${CATEGORIES.join(', ')}` });
  }

  // Validate priority enum
  if (data.priority !== undefined && !PRIORITIES.includes(data.priority)) {
    errors.push({ field: 'priority', message: `priority must be one of: ${PRIORITIES.join(', ')}` });
  }

  // Validate status enum
  if (data.status !== undefined && !STATUSES.includes(data.status)) {
    errors.push({ field: 'status', message: `status must be one of: ${STATUSES.join(', ')}` });
  }

  // Validate tags is array
  if (data.tags !== undefined && !Array.isArray(data.tags)) {
    errors.push({ field: 'tags', message: 'tags must be an array' });
  }

  // Validate metadata
  if (data.metadata !== undefined) {
    if (typeof data.metadata !== 'object' || Array.isArray(data.metadata)) {
      errors.push({ field: 'metadata', message: 'metadata must be an object' });
    } else {
      if (data.metadata.source !== undefined && !SOURCES.includes(data.metadata.source)) {
        errors.push({ field: 'metadata.source', message: `metadata.source must be one of: ${SOURCES.join(', ')}` });
      }
      if (data.metadata.device_type !== undefined && !DEVICE_TYPES.includes(data.metadata.device_type)) {
        errors.push({ field: 'metadata.device_type', message: `metadata.device_type must be one of: ${DEVICE_TYPES.join(', ')}` });
      }
    }
  }

  return errors;
}

/**
 * Creates a new ticket object with defaults
 */
function createTicket(data) {
  const now = new Date().toISOString();
  return {
    id: uuidv4(),
    customer_id: data.customer_id,
    customer_email: data.customer_email,
    customer_name: data.customer_name,
    subject: data.subject,
    description: data.description,
    category: data.category || 'other',
    priority: data.priority || 'medium',
    status: data.status || 'new',
    created_at: now,
    updated_at: now,
    resolved_at: null,
    assigned_to: data.assigned_to || null,
    tags: data.tags || [],
    metadata: {
      source: data.metadata?.source || 'api',
      browser: data.metadata?.browser || null,
      device_type: data.metadata?.device_type || null
    }
  };
}

/**
 * Updates a ticket with new data
 */
function updateTicket(ticket, data) {
  const updated = { ...ticket };
  const now = new Date().toISOString();

  // Update allowed fields
  if (data.customer_id !== undefined) updated.customer_id = data.customer_id;
  if (data.customer_email !== undefined) updated.customer_email = data.customer_email;
  if (data.customer_name !== undefined) updated.customer_name = data.customer_name;
  if (data.subject !== undefined) updated.subject = data.subject;
  if (data.description !== undefined) updated.description = data.description;
  if (data.category !== undefined) updated.category = data.category;
  if (data.priority !== undefined) updated.priority = data.priority;
  if (data.assigned_to !== undefined) updated.assigned_to = data.assigned_to;
  if (data.tags !== undefined) updated.tags = data.tags;

  // Handle status changes
  if (data.status !== undefined) {
    updated.status = data.status;
    if (data.status === 'resolved' || data.status === 'closed') {
      updated.resolved_at = updated.resolved_at || now;
    }
  }

  // Handle metadata
  if (data.metadata !== undefined) {
    updated.metadata = {
      ...updated.metadata,
      ...data.metadata
    };
  }

  updated.updated_at = now;
  return updated;
}

module.exports = {
  CATEGORIES,
  PRIORITIES,
  STATUSES,
  SOURCES,
  DEVICE_TYPES,
  validateTicket,
  createTicket,
  updateTicket,
  isValidEmail
};
