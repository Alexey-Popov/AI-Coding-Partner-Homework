const { z } = require("zod");

const ticketSchema = z.object({
  customer_id: z.string().min(1, "customer_id required"),
  customer_email: z.string().email("Invalid email format"),
  customer_name: z.string().min(1, "customer_name required"),
  subject: z.string().min(1, "subject required").max(200, "subject max 200 chars"),
  description: z.string().min(10, "description min 10 chars").max(2000, "description max 2000 chars"),
  category: z.enum([
    "account_access",
    "technical_issue",
    "billing_question",
    "feature_request",
    "bug_report",
    "other"
  ]).optional().default("other"),
  priority: z.enum(["urgent", "high", "medium", "low"]).optional().default("medium"),
  status: z.enum(["new", "in_progress", "waiting_customer", "resolved", "closed"]).optional().default("new"),
  assigned_to: z.string().nullable().optional(),
  tags: z.array(z.string()).optional().default([]),
  metadata: z.object({
    source: z.enum(["web_form", "email", "api", "chat", "phone"]).optional().default("api"),
    browser: z.string().optional().default("unknown"),
    device_type: z.enum(["desktop", "mobile", "tablet"]).optional().default("desktop")
  }).optional()
});

function validateTicket(payload) {
  try {
    return { valid: true, data: ticketSchema.parse(payload), errors: null };
  } catch (err) {
    return { valid: false, data: null, errors: err.errors.map((e) => ({
      field: e.path.join("."),
      message: e.message
    })) };
  }
}

module.exports = {
  ticketSchema,
  validateTicket
};
