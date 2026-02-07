ALTER TABLE tickets ADD COLUMN customer_id VARCHAR(255);
ALTER TABLE tickets ADD COLUMN resolved_at TIMESTAMP;
ALTER TABLE tickets ADD COLUMN assigned_to VARCHAR(255);
ALTER TABLE tickets ADD COLUMN last_classified_at TIMESTAMP;

CREATE TABLE ticket_classification_keywords (
    ticket_id UUID NOT NULL,
    keyword VARCHAR(255),
    CONSTRAINT fk_ticket_classification_keywords FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);

CREATE INDEX idx_tickets_assigned_to ON tickets(assigned_to);
