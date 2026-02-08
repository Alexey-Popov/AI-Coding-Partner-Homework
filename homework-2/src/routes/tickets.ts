import { Router, Request, Response, NextFunction } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { validate as isValidUUID } from 'uuid';
import multer from 'multer';
import { Ticket } from '../models/Ticket';
import { safeValidateCreateTicket, safeValidateUpdateTicket } from '../models/TicketValidator';
import { ClassificationService } from '../services/ClassificationService';
import { CsvImportService } from '../services/CsvImportService';
import { JsonImportService } from '../services/JsonImportService';
import { XmlImportService } from '../services/XmlImportService';

const router = Router();
const upload = multer({ dest: 'uploads/' });

const tickets: Ticket[] = [];
const classificationService = new ClassificationService();
const csvImportService = new CsvImportService();
const jsonImportService = new JsonImportService();
const xmlImportService = new XmlImportService();

function findTicketById(id: string): Ticket | undefined {
  return tickets.find((ticket) => ticket.id === id);
}

/**
 * POST /tickets - Create a new ticket
 * @returns 201 Created with ticket data
 */
router.post('/', (req: Request, res: Response, next: NextFunction): void => {
  try {
    // Validate input
    const validation = safeValidateCreateTicket(req.body);

    if (!validation.success) {
      res.status(400).json({
        success: false,
        error: 'Validation failed',
        details: validation.error.issues.map((issue) => ({
          field: issue.path.join('.'),
          message: issue.message,
        })),
      });
      return;
    }

    const ticketData = validation.data;
    const now = new Date();

    // Auto-classify ticket based on subject and description
    const classification = classificationService.classify(
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
      assigned_to: ticketData.assigned_to,
      tags: ticketData.tags ?? [],
      metadata: ticketData.metadata,
    };

    tickets.push(newTicket);

    res.status(201).json({
      success: true,
      data: newTicket,
      classification: {
        confidence: classification.confidence,
        reasoning: classification.reasoning,
        keywords: classification.keywords,
      },
    });
  } catch (error) {
    next(error);
  }
});

/**
 * POST /tickets/import - Import tickets from CSV, JSON, or XML file
 * @returns 200 OK with import results
 */
router.post(
  '/import',
  upload.single('file'),
  async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      if (!req.file) {
        res.status(400).json({
          success: false,
          error: 'No file uploaded',
          details: { message: 'Please upload a file using the "file" field' },
        });
        return;
      }

      const fileName = req.file.originalname.toLowerCase();
      let importResult: any;
      let fileType: string;

      if (fileName.endsWith('.csv')) {
        fileType = 'CSV';
        importResult = await csvImportService.importFromFile(req.file.path);
      } else if (fileName.endsWith('.json')) {
        fileType = 'JSON';
        importResult = await jsonImportService.importFromFile(req.file.path);
      } else if (fileName.endsWith('.xml')) {
        fileType = 'XML';
        importResult = await xmlImportService.importFromFile(req.file.path);
      } else {
        res.status(400).json({
          success: false,
          error: 'Invalid file type',
          details: { message: 'Only CSV, JSON, and XML files are allowed' },
        });
        return;
      }

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

      const now = new Date();
      const createdTickets: Ticket[] = [];

      importResult.validTickets.forEach((ticketData: any) => {
        const classification = classificationService.classify(
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
        };

        tickets.push(newTicket);
        createdTickets.push(newTicket);
      });

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
  }
);

/**
 * GET /tickets - Get all tickets with optional filtering
 * @query status - Filter by ticket status
 * @query priority - Filter by ticket priority
 * @query category - Filter by ticket category
 * @query limit - Maximum number of results
 * @query offset - Number of results to skip
 * @returns 200 OK with array of tickets
 */
router.get('/', (req: Request, res: Response, next: NextFunction): void => {
  try {
    const { status, priority, category, limit, offset } = req.query;

    let filteredTickets = [...tickets];

    // Filter by status
    if (status && typeof status === 'string') {
      filteredTickets = filteredTickets.filter((ticket) => ticket.status === status);
    }

    if (priority && typeof priority === 'string') {
      filteredTickets = filteredTickets.filter((ticket) => ticket.priority === priority);
    }

    if (category && typeof category === 'string') {
      filteredTickets = filteredTickets.filter((ticket) => ticket.category === category);
    }

    // Handle pagination
    const limitNum = limit && typeof limit === 'string' ? parseInt(limit, 10) : undefined;
    const offsetNum = offset && typeof offset === 'string' ? parseInt(offset, 10) : 0;

    if (limitNum !== undefined && !isNaN(limitNum) && !isNaN(offsetNum)) {
      filteredTickets = filteredTickets.slice(offsetNum, offsetNum + limitNum);
    } else if (!isNaN(offsetNum)) {
      filteredTickets = filteredTickets.slice(offsetNum);
    }

    res.status(200).json({
      success: true,
      data: filteredTickets,
      meta: {
        total: tickets.length,
        filtered: filteredTickets.length,
      },
    });
  } catch (error) {
    next(error);
  }
});

/**
 * GET /tickets/:id - Get a single ticket by ID
 * @param id - UUID of the ticket
 * @returns 200 OK with ticket data or 404 Not Found
 */
router.get('/:id', (req: Request, res: Response, next: NextFunction): void => {
  try {
    const { id } = req.params;

    // Validate UUID format
    if (!isValidUUID(id!)) {
      res.status(400).json({
        success: false,
        error: 'Invalid ticket ID format',
        details: { field: 'id', message: 'Must be a valid UUID' },
      });
      return;
    }

    const ticket = findTicketById(id!);

    if (!ticket) {
      res.status(404).json({
        success: false,
        error: 'Ticket not found',
        details: { id },
      });
      return;
    }

    res.status(200).json({
      success: true,
      data: ticket,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * PUT /tickets/:id - Update an existing ticket
 * @param id - UUID of the ticket
 * @returns 200 OK with updated ticket or 404 Not Found
 */
router.put('/:id', (req: Request, res: Response, next: NextFunction): void => {
  try {
    const { id } = req.params;

    // Validate UUID format
    if (!isValidUUID(id!)) {
      res.status(400).json({
        success: false,
        error: 'Invalid ticket ID format',
      });
      return;
    }

    const ticketIndex = tickets.findIndex((ticket) => ticket.id === id);

    if (ticketIndex === -1) {
      res.status(404).json({
        success: false,
        error: 'Ticket not found',
        details: { id },
      });
      return;
    }

    // Validate update data
    const validation = safeValidateUpdateTicket(req.body);

    if (!validation.success) {
      res.status(400).json({
        success: false,
        error: 'Validation failed',
        details: validation.error.issues.map((issue) => ({
          field: issue.path.join('.'),
          message: issue.message,
        })),
      });
      return;
    }

    const updateData = validation.data;
    const existingTicket = tickets[ticketIndex]!;

    // Check if status is being changed to 'resolved' or 'closed'
    const now = new Date();
    const resolvedAt =
      updateData.status &&
      (updateData.status === 'resolved' || updateData.status === 'closed') &&
      existingTicket.status !== 'resolved' &&
      existingTicket.status !== 'closed'
        ? now
        : existingTicket.resolved_at;

    const updatedTicket: Ticket = {
      ...existingTicket,
      ...updateData,
      updated_at: now,
      resolved_at: resolvedAt,
    };

    tickets[ticketIndex] = updatedTicket;

    res.status(200).json({
      success: true,
      data: updatedTicket,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * DELETE /tickets/:id - Delete a ticket
 * @param id - UUID of the ticket
 * @returns 200 OK or 404 Not Found
 */
router.delete('/:id', (req: Request, res: Response, next: NextFunction): void => {
  try {
    const { id } = req.params;

    // Validate UUID format
    if (!isValidUUID(id!)) {
      res.status(400).json({
        success: false,
        error: 'Invalid ticket ID format',
        details: { field: 'id', message: 'Must be a valid UUID' },
      });
      return;
    }

    const ticketIndex = tickets.findIndex((ticket) => ticket.id === id);

    if (ticketIndex === -1) {
      res.status(404).json({
        success: false,
        error: 'Ticket not found',
        details: { id },
      });
      return;
    }

    // Remove the ticket
    const deletedTicket = tickets.splice(ticketIndex, 1)[0];

    res.status(200).json({
      success: true,
      message: 'Ticket deleted successfully',
      data: { id: deletedTicket?.id },
    });
  } catch (error) {
    next(error);
  }
});
export default router;
