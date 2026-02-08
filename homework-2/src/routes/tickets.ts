import { Router } from 'express';
import multer from 'multer';
import { TicketController } from '../controllers/ticket.controller';
import { ImportController } from '../controllers/import.controller';
import { ClassificationService } from '../services/ClassificationService';
import { CsvImportService } from '../services/CsvImportService';
import { JsonImportService } from '../services/JsonImportService';
import { XmlImportService } from '../services/XmlImportService';
import { ticketRepository } from '../repositories/ticket.repository';

const router = Router();
const upload = multer({ dest: 'uploads/' });

const classificationService = new ClassificationService();

const ticketController = new TicketController(ticketRepository, classificationService);

const importController = new ImportController(
    ticketRepository,
    classificationService,
    new CsvImportService(),
    new JsonImportService(),
    new XmlImportService()
);

router.post('/', ticketController.createTicket);
router.post('/import', upload.single('file'), importController.importTickets);
router.post('/:id/auto-classify', ticketController.autoClassifyTicket);
router.get('/', ticketController.getAllTickets);
router.get('/:id', ticketController.getTicketById);
router.put('/:id', ticketController.updateTicket);
router.delete('/:id', ticketController.deleteTicket);

export default router;
