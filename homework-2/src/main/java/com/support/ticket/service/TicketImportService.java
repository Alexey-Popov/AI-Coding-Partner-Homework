package com.support.ticket.service;

import com.support.ticket.dto.BulkImportResponse;
import com.support.ticket.dto.CreateTicketRequest;
import com.support.ticket.exception.ImportException;
import com.support.ticket.service.parser.CsvImportParser;
import com.support.ticket.service.parser.JsonImportParser;
import com.support.ticket.service.parser.XmlImportParser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TicketImportService {

    private final CsvImportParser csvParser;
    private final JsonImportParser jsonParser;
    private final XmlImportParser xmlParser;
    private final TicketService ticketService;
    private final Validator validator;

    public TicketImportService(CsvImportParser csvParser, JsonImportParser jsonParser,
                               XmlImportParser xmlParser, TicketService ticketService,
                               Validator validator) {
        this.csvParser = csvParser;
        this.jsonParser = jsonParser;
        this.xmlParser = xmlParser;
        this.ticketService = ticketService;
        this.validator = validator;
    }

    public BulkImportResponse importTickets(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new ImportException("File name is required");
        }

        List<CreateTicketRequest> requests;
        try {
            if (filename.endsWith(".csv")) {
                requests = csvParser.parse(file.getInputStream());
            } else if (filename.endsWith(".json")) {
                requests = jsonParser.parse(file.getInputStream());
            } else if (filename.endsWith(".xml")) {
                requests = xmlParser.parse(file.getInputStream());
            } else {
                throw new ImportException("Unsupported file format. Supported: csv, json, xml");
            }
        } catch (IOException e) {
            throw new ImportException("Failed to read file: " + e.getMessage(), e);
        }

        int total = requests.size();
        int successful = 0;
        List<BulkImportResponse.ImportError> errors = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            CreateTicketRequest request = requests.get(i);
            Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);

            if (violations.isEmpty()) {
                try {
                    ticketService.createTicket(request);
                    successful++;
                } catch (Exception e) {
                    errors.add(new BulkImportResponse.ImportError(i + 1, "general", e.getMessage()));
                }
            } else {
                for (ConstraintViolation<CreateTicketRequest> violation : violations) {
                    errors.add(new BulkImportResponse.ImportError(
                            i + 1,
                            violation.getPropertyPath().toString(),
                            violation.getMessage()
                    ));
                }
            }
        }

        return new BulkImportResponse(total, successful, total - successful, errors);
    }
}
