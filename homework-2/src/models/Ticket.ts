export type TicketCategory =
    | 'account_access'
    | 'technical_issue'
    | 'billing_question'
    | 'feature_request'
    | 'bug_report'
    | 'other';

export type TicketPriority = 'urgent' | 'high' | 'medium' | 'low';

export type TicketStatus = 'new' | 'in_progress' | 'waiting_customer' | 'resolved' | 'closed';

export type TicketSource = 'web_form' | 'email' | 'api' | 'chat' | 'phone';

export type DeviceType = 'desktop' | 'mobile' | 'tablet';

export interface TicketMetadata {
    source: TicketSource;
    browser?: string | null;
    device_type: DeviceType;
}

export interface Ticket {
    id: string;
    customer_id: string;
    customer_email: string;
    customer_name: string;
    subject: string;
    description: string;
    category: TicketCategory;
    priority: TicketPriority;
    status: TicketStatus;
    created_at: Date;
    updated_at: Date;
    resolved_at: Date | null;
    assigned_to: string | null;
    tags: string[];
    metadata: TicketMetadata;
}

export type CreateTicketInput = Omit<
    Ticket,
    'id' | 'created_at' | 'updated_at' | 'resolved_at' | 'status' | 'category' | 'priority'
>;

export type UpdateTicketInput = Partial<Omit<Ticket, 'id' | 'created_at' | 'customer_id'>>;
