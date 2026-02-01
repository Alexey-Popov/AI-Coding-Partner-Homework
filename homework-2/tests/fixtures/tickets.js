const validTicket = {
  customer_id: "CUST001",
  customer_email: "john.doe@example.com",
  customer_name: "John Doe",
  subject: "Cannot login to my account",
  description: "I have been trying to login for the past hour but keep getting an error message. Please help me regain access to my account immediately.",
  category: "account_access",
  priority: "urgent",
  status: "new",
  tags: ["urgent", "account"]
};

const validTicket2 = {
  customer_id: "CUST002",
  customer_email: "jane.smith@example.com",
  customer_name: "Jane Smith",
  subject: "Billing discrepancy on invoice",
  description: "I noticed I was charged twice for my last subscription renewal. Can you please review and refund the duplicate charge?",
  category: "billing_question",
  priority: "high",
  status: "new",
  tags: ["billing"]
};

const validTicket3 = {
  customer_id: "CUST003",
  customer_email: "bob.wilson@example.com",
  customer_name: "Bob Wilson",
  subject: "Feature suggestion for mobile app",
  description: "It would be great if the mobile app had offline mode so I can access my documents even without internet connection.",
  category: "feature_request",
  priority: "low",
  status: "new",
  tags: ["feature", "mobile"]
};

const invalidTicketMissingEmail = {
  customer_id: "CUST004",
  customer_email: "not-an-email",
  customer_name: "Invalid User",
  subject: "Test",
  description: "This ticket has invalid email"
};

const invalidTicketShortDescription = {
  customer_id: "CUST005",
  customer_email: "test@example.com",
  customer_name: "Test User",
  subject: "Test",
  description: "Short"
};

const invalidTicketMissingRequired = {
  customer_email: "test@example.com",
  subject: "Missing customer_id"
};

const ticketForClassification = {
  customer_id: "CUST006",
  customer_email: "critical@example.com",
  customer_name: "Critical Issue",
  subject: "Production down - security breach detected",
  description: "Our production system is down due to a critical security vulnerability. Can't access any customer data.",
  category: "other",
  priority: "low"
};

module.exports = {
  validTicket,
  validTicket2,
  validTicket3,
  invalidTicketMissingEmail,
  invalidTicketShortDescription,
  invalidTicketMissingRequired,
  ticketForClassification
};
