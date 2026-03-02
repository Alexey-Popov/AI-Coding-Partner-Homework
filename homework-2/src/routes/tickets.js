const express = require("express");
const multer = require("multer");
const { validateTicket } = require("../models/ticket");
const store = require("../store");
const { autoClassify } = require("../services/classifier");
const { importFromFile } = require("../services/importer");

const router = express.Router();
const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: 5 * 1024 * 1024 } });

// POST /tickets - Create a new ticket
router.post("/", (req, res) => {
  const validation = validateTicket(req.body);
  if (!validation.valid) {
    return res.status(400).json({ error: "Validation failed", details: validation.errors });
  }

  const ticket = store.createTicket(validation.data);
  res.status(201).json(ticket);
});

// GET /tickets - List all tickets with optional filtering
router.get("/", (req, res) => {
  const filters = {
    status: req.query.status,
    category: req.query.category,
    priority: req.query.priority,
    customer_id: req.query.customer_id,
    assigned_to: req.query.assigned_to
  };

  Object.keys(filters).forEach((key) => {
    if (!filters[key]) delete filters[key];
  });

  const tickets = store.listTickets(filters);
  res.status(200).json({ total: tickets.length, tickets });
});

// GET /tickets/:id - Get a specific ticket
router.get("/:id", (req, res) => {
  const ticket = store.getTicket(req.params.id);
  if (!ticket) {
    return res.status(404).json({ error: "Ticket not found" });
  }
  res.status(200).json(ticket);
});

// PUT /tickets/:id - Update a ticket
router.put("/:id", (req, res) => {
  const ticket = store.getTicket(req.params.id);
  if (!ticket) {
    return res.status(404).json({ error: "Ticket not found" });
  }

  const updates = {};
  ["customer_id", "customer_email", "customer_name", "subject", "description", "category", "priority", "status", "assigned_to", "tags"].forEach((field) => {
    if (req.body[field] !== undefined) {
      updates[field] = req.body[field];
    }
  });

  const validation = validateTicket({ ...ticket, ...updates });
  if (!validation.valid) {
    return res.status(400).json({ error: "Validation failed", details: validation.errors });
  }

  const updated = store.updateTicket(req.params.id, updates);
  res.status(200).json(updated);
});

// DELETE /tickets/:id - Delete a ticket
router.delete("/:id", (req, res) => {
  const ticket = store.getTicket(req.params.id);
  if (!ticket) {
    return res.status(404).json({ error: "Ticket not found" });
  }

  store.deleteTicket(req.params.id);
  res.status(204).send();
});

// POST /tickets/import - Bulk import from CSV/JSON/XML
router.post("/import", upload.single("file"), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: "No file uploaded" });
  }

  const fileType = req.query.type || req.file.originalname.split(".").pop().toLowerCase();
  let records;
  try {
    records = importFromFile(req.file.buffer, fileType);
  } catch (err) {
    return res.status(400).json({ error: "Import parsing failed", details: err.message });
  }

  const results = [];
  records.forEach((record, index) => {
    const validation = validateTicket(record);
    if (validation.valid) {
      const ticket = store.createTicket(validation.data);
      results.push({ index, success: true, id: ticket.id });
    } else {
      results.push({ index, success: false, errors: validation.errors });
    }
  });

  const successful = results.filter((r) => r.success).length;
  const failed = results.filter((r) => !r.success).length;

  res.status(202).json({
    summary: {
      total: records.length,
      successful,
      failed
    },
    results
  });
});

// POST /tickets/:id/auto-classify - Auto-classify a ticket
router.post("/:id/auto-classify", (req, res) => {
  const ticket = store.getTicket(req.params.id);
  if (!ticket) {
    return res.status(404).json({ error: "Ticket not found" });
  }

  const classification = autoClassify(ticket);
  const allowOverride = req.body.allow_override !== false;

  if (allowOverride) {
    const updates = {};
    if (req.body.category) updates.category = req.body.category;
    if (req.body.priority) updates.priority = req.body.priority;
    store.updateTicket(req.params.id, updates);
  }

  store.logClassification(req.params.id, classification);

  res.status(200).json({
    ticket_id: ticket.id,
    ...classification,
    allow_override: allowOverride
  });
});

module.exports = router;
