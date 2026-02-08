import { z } from 'zod';

export const TicketCategorySchema = z.enum([
    'account_access',
    'technical_issue',
    'billing_question',
    'feature_request',
    'bug_report',
    'other',
]);

export const TicketPrioritySchema = z.enum(['urgent', 'high', 'medium', 'low']);

export const TicketStatusSchema = z.enum([
    'new',
    'in_progress',
    'waiting_customer',
    'resolved',
    'closed',
]);

export const TicketSourceSchema = z.enum(['web_form', 'email', 'api', 'chat', 'phone']);

export const DeviceTypeSchema = z.enum(['desktop', 'mobile', 'tablet']);

export const TicketMetadataSchema = z.object({
    source: TicketSourceSchema,
    browser: z.string().min(1, 'Browser information is required'),
    device_type: DeviceTypeSchema,
});

export const TicketSchema = z.object({
    id: z.string().uuid(),
    customer_id: z.string().min(1, 'Customer ID is required'),
    customer_email: z.string().email(),
    customer_name: z.string().min(1, 'Customer name is required'),
    subject: z
        .string()
        .min(1, 'Subject must be at least 1 character')
        .max(200, 'Subject must not exceed 200 characters'),
    description: z
        .string()
        .min(10, 'Description must be at least 10 characters')
        .max(2000, 'Description must not exceed 2000 characters'),
    category: TicketCategorySchema,
    priority: TicketPrioritySchema,
    status: TicketStatusSchema,
    created_at: z.coerce.date(),
    updated_at: z.coerce.date(),
    resolved_at: z.coerce.date().nullable(),
    assigned_to: z.string().nullable(),
    tags: z.array(z.string()),
    metadata: TicketMetadataSchema,
});

export const CreateTicketSchema = z.object({
    customer_id: z.string().min(1, 'Customer ID is required'),
    customer_email: z.string().email(),
    customer_name: z.string().min(1, 'Customer name is required'),
    subject: z
        .string()
        .min(1, 'Subject must be at least 1 character')
        .max(200, 'Subject must not exceed 200 characters'),
    description: z
        .string()
        .min(10, 'Description must be at least 10 characters')
        .max(2000, 'Description must not exceed 2000 characters'),
    assigned_to: z.string().nullable().optional().default(null),
    tags: z.array(z.string()).optional().default([]),
    metadata: TicketMetadataSchema,
});

export const UpdateTicketSchema = z.object({
    customer_email: z.string().email().optional(),
    customer_name: z.string().min(1, 'Customer name is required').optional(),
    subject: z
        .string()
        .min(1, 'Subject must be at least 1 character')
        .max(200, 'Subject must not exceed 200 characters')
        .optional(),
    description: z
        .string()
        .min(10, 'Description must be at least 10 characters')
        .max(2000, 'Description must not exceed 2000 characters')
        .optional(),
    category: TicketCategorySchema.optional(),
    priority: TicketPrioritySchema.optional(),
    status: TicketStatusSchema.optional(),
    updated_at: z.coerce.date().optional(),
    resolved_at: z.coerce.date().nullable().optional(),
    assigned_to: z.string().nullable().optional(),
    tags: z.array(z.string()).optional(),
    metadata: TicketMetadataSchema.optional(),
});

export type TicketSchemaType = z.infer<typeof TicketSchema>;
export type CreateTicketSchemaType = z.infer<typeof CreateTicketSchema>;
export type UpdateTicketSchemaType = z.infer<typeof UpdateTicketSchema>;

export function validateTicket(data: unknown): TicketSchemaType {
    return TicketSchema.parse(data);
}

export function validateCreateTicket(data: unknown): CreateTicketSchemaType {
    return CreateTicketSchema.parse(data);
}

export function validateUpdateTicket(data: unknown): UpdateTicketSchemaType {
    return UpdateTicketSchema.parse(data);
}

export function safeValidateTicket(data: unknown) {
    return TicketSchema.safeParse(data);
}

export function safeValidateCreateTicket(data: unknown) {
    return CreateTicketSchema.safeParse(data);
}

export function safeValidateUpdateTicket(data: unknown) {
    return UpdateTicketSchema.safeParse(data);
}
