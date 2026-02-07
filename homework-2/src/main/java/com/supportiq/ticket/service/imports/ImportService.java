package com.supportiq.ticket.service.imports;

import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.dto.response.ImportFailureDto;
import com.supportiq.ticket.dto.response.ImportResultDto;
import com.supportiq.ticket.dto.response.TicketDto;
import com.supportiq.ticket.exception.MalformedImportFileException;
import com.supportiq.ticket.exception.UnsupportedImportFormatException;
import com.supportiq.ticket.service.TicketService;
import com.supportiq.ticket.service.classification.ClassificationResult;
import com.supportiq.ticket.service.classification.ClassificationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final CsvImportParser csvParser;
    private final JsonImportParser jsonParser;
    private final XmlImportParser xmlParser;
    private final ImportTransactionHelper transactionHelper;
    private final ClassificationService classificationService;
    private final TicketService ticketService;
    private final Validator validator;

    public ImportService(CsvImportParser csvParser, JsonImportParser jsonParser,
                         XmlImportParser xmlParser, ImportTransactionHelper transactionHelper,
                         ClassificationService classificationService, TicketService ticketService,
                         Validator validator) {
        this.csvParser = csvParser;
        this.jsonParser = jsonParser;
        this.xmlParser = xmlParser;
        this.transactionHelper = transactionHelper;
        this.classificationService = classificationService;
        this.ticketService = ticketService;
        this.validator = validator;
    }

    public ImportResultDto importTickets(MultipartFile file, boolean autoClassify) {
        String format = detectFormat(file);
        List<CreateTicketRequest> requests;

        try {
            requests = switch (format) {
                case "csv" -> csvParser.parse(file.getInputStream());
                case "json" -> jsonParser.parse(file.getInputStream());
                case "xml" -> xmlParser.parse(file.getInputStream());
                default -> throw new UnsupportedImportFormatException(format);
            };
        } catch (IOException e) {
            throw new MalformedImportFileException("Failed to read import file: " + e.getMessage());
        }

        ImportResultDto result = new ImportResultDto();
        result.setTotalRecords(requests.size());

        List<TicketDto> createdTickets = new ArrayList<>();
        List<ImportFailureDto> failures = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            CreateTicketRequest request = requests.get(i);
            List<String> errors = validateRequest(request);

            if (!errors.isEmpty()) {
                failures.add(new ImportFailureDto(i, errors));
                continue;
            }

            try {
                if (autoClassify && request.getCategory() == null) {
                    ClassificationResult classification = classificationService.classify(
                            request.getSubject(), request.getDescription());
                    request.setCategory(classification.category());
                    request.setPriority(classification.priority());
                }

                TicketDto ticket = transactionHelper.saveTicket(request);
                createdTickets.add(ticket);
            } catch (Exception e) {
                log.warn("Failed to import record {}: {}", i, e.getMessage());
                failures.add(new ImportFailureDto(i, List.of(e.getMessage())));
            }
        }

        result.setSuccessful(createdTickets.size());
        result.setFailed(failures.size());
        result.setCreatedTickets(createdTickets);
        result.setFailures(failures);

        return result;
    }

    private String detectFormat(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new UnsupportedImportFormatException("unknown");
        }

        String lower = filename.toLowerCase();
        if (lower.endsWith(".csv")) return "csv";
        if (lower.endsWith(".json")) return "json";
        if (lower.endsWith(".xml")) return "xml";

        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.contains("csv")) return "csv";
            if (contentType.contains("json")) return "json";
            if (contentType.contains("xml")) return "xml";
        }

        throw new UnsupportedImportFormatException(filename);
    }

    private List<String> validateRequest(CreateTicketRequest request) {
        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();
    }
}
