import { Request, Response, NextFunction } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { Ticket } from '../models/Ticket';
import { ClassificationService } from '../services/ClassificationService';
import { CsvImportService } from '../services/CsvImportService';
import { JsonImportService } from '../services/JsonImportService';
import { XmlImportService } from '../services/XmlImportService';
import { TicketRepository } from '../repositories/ticket.repository';

export class ImportController {
    private repository: TicketRepository;
    private classificationService: ClassificationService;
    private csvImportService: CsvImportService;
    private jsonImportService: JsonImportService;
    private xmlImportService: XmlImportService;

    constructor(
        repository: TicketRepository,
        classificationService: ClassificationService,
        csvImportService: CsvImportService,
        jsonImportService: JsonImportService,
        xmlImportService: XmlImportService
    ) {
        this.repository = repository;
        this.classificationService = classificationService;
        this.csvImportService = csvImportService;
        this.jsonImportService = jsonImportService;
        this.xmlImportService = xmlImportService;
    }

    importTickets = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
        try {
            if (!req.file) {
                res.status(400).json({
                    success: false,
                    error: 'No file uploaded',
                    details: { message: 'Please upload a file using the "file" field' },
                });
                return;
            }

            const { service, fileType } = this.getImportService(req.file.originalname);

            if (!service) {
                res.status(400).json({
                    success: false,
                    error: 'Invalid file type',
                    details: { message: 'Only CSV, JSON, and XML files are allowed' },
                });
                return;
            }

            const importResult = await service.importFromFile(req.file.path);

            if (!importResult.success) {
                res.status(400).json({
                    success: false,
                    error: `${fileType} import failed`,
                    imported: importResult.imported,
                    failed: importResult.failed,
                    errors: importResult.errors,
                });
                return;
            }

            const createdTickets = this.createTicketsFromImport(importResult.validTickets);

            res.status(200).json({
                success: true,
                message: `${fileType} import completed successfully`,
                format: fileType,
                imported: importResult.imported,
                failed: importResult.failed,
                errors: importResult.errors,
                tickets: createdTickets,
            });
        } catch (error) {
            next(error);
        }
    };

    private getImportService(
        filename: string
    ): { service: any; fileType: string } | { service: null; fileType: null } {
        const lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith('.csv')) {
            return { service: this.csvImportService, fileType: 'CSV' };
        }

        if (lowerFilename.endsWith('.json')) {
            return { service: this.jsonImportService, fileType: 'JSON' };
        }

        if (lowerFilename.endsWith('.xml')) {
            return { service: this.xmlImportService, fileType: 'XML' };
        }

        return { service: null, fileType: null };
    }

    private createTicketsFromImport(validTickets: any[]): Ticket[] {
        const now = new Date();
        const createdTickets: Ticket[] = [];

        validTickets.forEach((ticketData) => {
            const classification = this.classificationService.classify(
                ticketData.subject,
                ticketData.description
            );

            const newTicket: Ticket = {
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
                assigned_to: ticketData.assigned_to ?? null,
                tags: ticketData.tags ?? [],
                metadata: ticketData.metadata,
                classification_source: 'automatic',
            };

            createdTickets.push(newTicket);
        });

        this.repository.addBulk(createdTickets);
        return createdTickets;
    }
}
