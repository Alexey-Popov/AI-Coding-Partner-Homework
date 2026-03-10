const express = require('express');
const multer = require('multer');
const router = express.Router();
const { validateTicket, createTicket, updateTicket } = require('../models/ticket');
const {
  getAllTickets,
  getTicketById,
  addTicket,
  updateTicketInStore,
  deleteTicket,
  getFilteredTickets
} = require('../data/store');
const { autoClassify } = require('../services/classifier');
const { importTickets } = require('../services/importers');

// Configure multer for file uploads
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 } // 10MB limit
});

/**
 * POST /tickets
 * Create a new ticket
 */
router.post('/', (req, res) => {
  const errors = validateTicket(req.body);

  if (errors.length > 0) {
    return res.status(400).json({
      error: 'Validation failed',
      details: errors
    });
  }

  const ticket = createTicket(req.body);

  // Auto-classify if requested
  if (req.query.autoClassify === 'true') {
    const classification = autoClassify(ticket);
    ticket.category = classification.category;
    ticket.priority = classification.priority;
  }

  addTicket(ticket);
  res.status(201).json(ticket);
});

/**
 * POST /tickets/import
 * Bulk import tickets from CSV/JSON/XML
 */
router.post('/import', upload.single('file'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({
        error: 'No file uploaded',
        message: 'Please upload a file with the "file" field'
      });
    }

    // Determine format from query parameter or file extension
    let format = req.query.format;
    if (!format) {
      const filename = req.file.originalname.toLowerCase();
      if (filename.endsWith('.csv')) format = 'csv';
      else if (filename.endsWith('.json')) format = 'json';
      else if (filename.endsWith('.xml')) format = 'xml';
    }

    if (!format) {
      return res.status(400).json({
        error: 'Format not specified',
        message: 'Please specify format via query parameter (?format=csv|json|xml) or use appropriate file extension'
      });
    }

    const content = req.file.buffer.toString('utf-8');
    const autoClassifyTickets = req.query.autoClassify === 'true';

    const result = await importTickets(content, format, { autoClassifyTickets });

    res.status(200).json({
      message: 'Import completed',
      summary: {
        total: result.total,
        successful: result.successful,
        failed: result.failed
      },
      errors: result.errors.length > 0 ? result.errors : undefined
    });
  } catch (error) {
    res.status(400).json({
      error: 'Import failed',
      message: error.message
    });
  }
});

/**
 * GET /tickets
 * List all tickets with optional filtering
 */
router.get('/', (req, res) => {
  const { category, priority, status, assigned_to, customer_id, from, to } = req.query;

  // If any filters provided, use filtered query
  if (category || priority || status || assigned_to || customer_id || from || to) {
    const filtered = getFilteredTickets({
      category, priority, status, assigned_to, customer_id, from, to
    });
    return res.json(filtered);
  }

  res.json(getAllTickets());
});

/**
 * GET /tickets/:id
 * Get a specific ticket
 */
router.get('/:id', (req, res) => {
  const ticket = getTicketById(req.params.id);

  if (!ticket) {
    return res.status(404).json({
      error: 'Not found',
      message: `Ticket with ID ${req.params.id} not found`
    });
  }

  res.json(ticket);
});

/**
 * PUT /tickets/:id
 * Update a ticket
 */
router.put('/:id', (req, res) => {
  const ticket = getTicketById(req.params.id);

  if (!ticket) {
    return res.status(404).json({
      error: 'Not found',
      message: `Ticket with ID ${req.params.id} not found`
    });
  }

  const errors = validateTicket(req.body, true);

  if (errors.length > 0) {
    return res.status(400).json({
      error: 'Validation failed',
      details: errors
    });
  }

  const updated = updateTicket(ticket, req.body);
  updateTicketInStore(req.params.id, updated);

  res.json(updated);
});

/**
 * DELETE /tickets/:id
 * Delete a ticket
 */
router.delete('/:id', (req, res) => {
  const deleted = deleteTicket(req.params.id);

  if (!deleted) {
    return res.status(404).json({
      error: 'Not found',
      message: `Ticket with ID ${req.params.id} not found`
    });
  }

  res.status(204).send();
});

/**
 * POST /tickets/:id/auto-classify
 * Auto-classify a ticket
 */
router.post('/:id/auto-classify', (req, res) => {
  const ticket = getTicketById(req.params.id);

  if (!ticket) {
    return res.status(404).json({
      error: 'Not found',
      message: `Ticket with ID ${req.params.id} not found`
    });
  }

  const classification = autoClassify(ticket);

  // Update ticket with classification results unless override=false
  if (req.query.apply !== 'false') {
    const updated = updateTicket(ticket, {
      category: classification.category,
      priority: classification.priority
    });
    updateTicketInStore(req.params.id, updated);
  }

  res.json({
    ticket_id: ticket.id,
    category: classification.category,
    category_confidence: classification.category_confidence,
    priority: classification.priority,
    priority_confidence: classification.priority_confidence,
    reasoning: classification.reasoning,
    keywords_found: {
      category: classification.category_keywords,
      priority: classification.priority_keywords
    }
  });
});

module.exports = router;
