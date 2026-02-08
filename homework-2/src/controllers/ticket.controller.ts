import { Request, Response, NextFunction } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { Ticket } from '../models/Ticket';
import { safeValidateCreateTicket, safeValidateUpdateTicket } from '../models/TicketValidator';
import { ClassificationService } from '../services/ClassificationService';
import { TicketRepository, TicketFilters } from '../repositories/ticket.repository';
import { validateUUID, handleValidationError } from '../utils/validation.utils';
import { sendSuccess, sendCreated, sendNotFound, sendDeleted } from '../utils/response.utils';

export class TicketController {
    private repository: TicketRepository;
    private classificationService: ClassificationService;

    constructor(repository: TicketRepository, classificationService: ClassificationService) {
        this.repository = repository;
        this.classificationService = classificationService;
    }

    createTicket = (req: Request, res: Response, next: NextFunction): void => {
        try {
            const validation = safeValidateCreateTicket(req.body);

            if (!validation.success) {
                handleValidationError(res, validation.error.issues);
                return;
            }

            const ticketData = validation.data;
            const now = new Date();
            
            // Check if manual classification is provided
            const isManualClassification = ticketData.category && ticketData.priority;
            
            let category, priority, classificationSource;
            let classificationMetadata = null;

            if (isManualClassification) {
                // Manual override: use user-provided values
                category = ticketData.category!;
                priority = ticketData.priority!;
                classificationSource = 'manual' as const;
            } else {
                // Automatic classification
                const classification = this.classificationService.classify(
                    ticketData.subject,
                    ticketData.description
                );
                category = classification.category;
                priority = classification.priority;
                classificationSource = 'automatic' as const;
                classificationMetadata = {
                    confidence: classification.confidence,
                    reasoning: classification.reasoning,
                    keywords: classification.keywords,
                };
            }

            const newTicket = this.buildNewTicket(
                ticketData, 
                { category, priority, source: classificationSource }, 
                now
            );
            this.repository.create(newTicket);

            if (classificationMetadata) {
                sendCreated(res, newTicket, { classification: classificationMetadata });
            } else {
                sendCreated(res, newTicket);
            }
        } catch (error) {
            next(error);
        }
    };

    getAllTickets = (req: Request, res: Response, next: NextFunction): void => {
        try {
            const filters = this.extractFilters(req.query);
            const filteredTickets = this.repository.findWithFilters(filters);

            sendSuccess(res, filteredTickets);
        } catch (error) {
            next(error);
        }
    };

    getTicketById = (req: Request, res: Response, next: NextFunction): void => {
        try {
            const { id } = req.params;

            if (!id || !validateUUID(id, res)) {
                return;
            }

            const ticket = this.repository.findById(id);

            if (!ticket) {
                sendNotFound(res, id);
                return;
            }

            sendSuccess(res, ticket);
        } catch (error) {
            next(error);
        }
    };

    updateTicket = (req: Request, res: Response, next: NextFunction): void => {
        try {
            const { id } = req.params;

            if (!id || !validateUUID(id, res)) {
                return;
            }

            const existingTicket = this.repository.findById(id);

            if (!existingTicket) {
                sendNotFound(res, id);
                return;
            }

            const validation = safeValidateUpdateTicket(req.body);

            if (!validation.success) {
                handleValidationError(res, validation.error.issues);
                return;
            }

            const updatedTicket = this.buildUpdatedTicket(existingTicket, validation.data);
            this.repository.update(id, updatedTicket);

            sendSuccess(res, updatedTicket);
        } catch (error) {
            next(error);
        }
    };

    deleteTicket = (req: Request, res: Response, next: NextFunction): void => {
        try {
            const { id } = req.params;

            if (!id || !validateUUID(id, res)) {
                return;
            }

            const deletedTicket = this.repository.delete(id);

            if (!deletedTicket) {
                sendNotFound(res, id);
                return;
            }

            sendDeleted(res, deletedTicket.id);
        } catch (error) {
            next(error);
        }
    };

    private buildNewTicket(ticketData: any, classification: any, now: Date): Ticket {
        return {
            id: uuidv4(),
            customer_id: ticketData.customer_id,
            customer_email: ticketData.customer_email,
            customer_name: ticketData.customer_name,
            subject: ticketData.subject,
            description: ticketData.description,
            category: classification.category,
            priority: classification.priority,
            status: 'new',
            created_at: now,
            updated_at: now,
            resolved_at: null,
            assigned_to: null, // System-generated: always starts as null
            tags: [], // System-generated: always starts empty
            metadata: ticketData.metadata,
            classification_source: classification.source,
        };
    }

    private buildUpdatedTicket(existingTicket: Ticket, updateData: any): Ticket {
        const now = new Date();
        const shouldSetResolvedAt = this.shouldSetResolvedTimestamp(
            existingTicket.status,
            updateData.status
        );

        return {
            ...existingTicket,
            ...updateData,
            updated_at: now,
            resolved_at: shouldSetResolvedAt ? now : existingTicket.resolved_at,
        };
    }

    private shouldSetResolvedTimestamp(currentStatus: string, newStatus?: string): boolean {
        if (!newStatus) {
            return false;
        }

        const isBecomingResolved =
            (newStatus === 'resolved' || newStatus === 'closed') &&
            currentStatus !== 'resolved' &&
            currentStatus !== 'closed';

        return isBecomingResolved;
    }

    private extractFilters(query: any): TicketFilters {
        const filters: TicketFilters = {};

        if (query.status && typeof query.status === 'string') {
            filters.status = query.status as any;
        }

        if (query.priority && typeof query.priority === 'string') {
            filters.priority = query.priority as any;
        }

        if (query.category && typeof query.category === 'string') {
            filters.category = query.category as any;
        }

        if (query.limit && typeof query.limit === 'string') {
            const limitNum = parseInt(query.limit, 10);
            if (!isNaN(limitNum)) {
                filters.limit = limitNum;
            }
        }

        if (query.offset && typeof query.offset === 'string') {
            const offsetNum = parseInt(query.offset, 10);
            if (!isNaN(offsetNum)) {
                filters.offset = offsetNum;
            }
        }

        return filters;
    }
}
