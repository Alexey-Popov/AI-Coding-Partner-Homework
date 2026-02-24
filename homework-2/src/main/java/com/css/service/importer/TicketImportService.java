package com.css.service.importer;

import com.css.dto.ImportResult;
import com.css.exception.ImportException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TicketImportService {

    private final CsvImportService csvImportService;
    private final JsonImportService jsonImportService;
    private final XmlImportService xmlImportService;

    public TicketImportService(CsvImportService csvImportService,
                                JsonImportService jsonImportService,
                                XmlImportService xmlImportService) {
        this.csvImportService = csvImportService;
        this.jsonImportService = jsonImportService;
        this.xmlImportService = xmlImportService;
    }

    public ImportResult importTickets(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new ImportException("File name is required");
        }

        String extension = getFileExtension(filename).toLowerCase();

        return switch (extension) {
            case "csv" -> csvImportService.importFromCsv(file);
            case "json" -> jsonImportService.importFromJson(file);
            case "xml" -> xmlImportService.importFromXml(file);
            default -> throw new ImportException("Unsupported file format: " + extension +
                    ". Supported formats: CSV, JSON, XML");
        };
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new ImportException("File must have an extension (csv, json, or xml)");
        }
        return filename.substring(lastDotIndex + 1);
    }
}

