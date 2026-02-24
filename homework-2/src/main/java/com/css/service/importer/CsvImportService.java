package com.css.service.importer;

import com.css.dto.ImportResult;
import com.css.model.DeviceType;
import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketMetadata;
import com.css.model.TicketPriority;
import com.css.model.TicketSource;
import com.css.model.TicketStatus;
import com.css.service.TicketService;
import com.css.service.TicketValidationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CsvImportService {

    private final TicketService ticketService;
    private final TicketValidationService validationService;

    public CsvImportService(TicketService ticketService, TicketValidationService validationService) {
        this.ticketService = ticketService;
        this.validationService = validationService;
    }

    public ImportResult importFromCsv(MultipartFile file) {
        ImportResult result = new ImportResult();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.setTotalRecords(0);
                return result;
            }

            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].toLowerCase().trim(), i);
            }

            String line;
            int recordNumber = 1;
            while ((line = reader.readLine()) != null) {
                recordNumber++;
                result.setTotalRecords(result.getTotalRecords() + 1);

                try {
                    String[] values = parseCsvLine(line);
                    Ticket ticket = parseTicketFromCsv(values, headerIndex, recordNumber, result);

                    if (ticket != null) {
                        Map<String, String> errors = validationService.validateTicketData(ticket);
                        if (errors.isEmpty()) {
                            Ticket saved = ticketService.saveTicket(ticket);
                            result.addImportedTicketId(saved.getId().toString());
                            result.setSuccessfulRecords(result.getSuccessfulRecords() + 1);
                        } else {
                            for (Map.Entry<String, String> error : errors.entrySet()) {
                                result.addError(new ImportResult.ImportError(
                                        recordNumber, error.getKey(), error.getValue(), line));
                            }
                            result.setFailedRecords(result.getFailedRecords() + 1);
                        }
                    }
                } catch (Exception e) {
                    result.addError(new ImportResult.ImportError(
                            recordNumber, "parsing", "Failed to parse record: " + e.getMessage(), line));
                    result.setFailedRecords(result.getFailedRecords() + 1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file: " + e.getMessage(), e);
        }

        return result;
    }

    private Ticket parseTicketFromCsv(String[] values, Map<String, Integer> headerIndex,
                                       int recordNumber, ImportResult result) {
        Ticket ticket = new Ticket();

        ticket.setCustomerId(getValue(values, headerIndex, "customer_id"));
        ticket.setCustomerEmail(getValue(values, headerIndex, "customer_email"));
        ticket.setCustomerName(getValue(values, headerIndex, "customer_name"));
        ticket.setSubject(getValue(values, headerIndex, "subject"));
        ticket.setDescription(getValue(values, headerIndex, "description"));

        String categoryStr = getValue(values, headerIndex, "category");
        if (categoryStr != null && !categoryStr.isEmpty()) {
            try {
                ticket.setCategory(TicketCategory.valueOf(categoryStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                ticket.setCategory(TicketCategory.OTHER);
            }
        } else {
            ticket.setCategory(TicketCategory.OTHER);
        }

        String priorityStr = getValue(values, headerIndex, "priority");
        if (priorityStr != null && !priorityStr.isEmpty()) {
            try {
                ticket.setPriority(TicketPriority.valueOf(priorityStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                ticket.setPriority(TicketPriority.MEDIUM);
            }
        } else {
            ticket.setPriority(TicketPriority.MEDIUM);
        }

        String statusStr = getValue(values, headerIndex, "status");
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                ticket.setStatus(TicketStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                ticket.setStatus(TicketStatus.NEW);
            }
        } else {
            ticket.setStatus(TicketStatus.NEW);
        }

        String assignedTo = getValue(values, headerIndex, "assigned_to");
        if (assignedTo != null && !assignedTo.isEmpty()) {
            ticket.setAssignedTo(assignedTo);
        }

        String tagsStr = getValue(values, headerIndex, "tags");
        if (tagsStr != null && !tagsStr.isEmpty()) {
            ticket.setTags(Arrays.asList(tagsStr.split("\\|")));
        }

        // Parse metadata
        TicketMetadata metadata = new TicketMetadata();
        String sourceStr = getValue(values, headerIndex, "source");
        if (sourceStr != null && !sourceStr.isEmpty()) {
            try {
                metadata.setSource(TicketSource.valueOf(sourceStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                metadata.setSource(TicketSource.API);
            }
        }
        metadata.setBrowser(getValue(values, headerIndex, "browser"));
        String deviceTypeStr = getValue(values, headerIndex, "device_type");
        if (deviceTypeStr != null && !deviceTypeStr.isEmpty()) {
            try {
                metadata.setDeviceType(DeviceType.valueOf(deviceTypeStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                metadata.setDeviceType(DeviceType.DESKTOP);
            }
        }
        ticket.setMetadata(metadata);

        return ticket;
    }

    private String getValue(String[] values, Map<String, Integer> headerIndex, String key) {
        Integer index = headerIndex.get(key);
        if (index != null && index < values.length) {
            return values[index].trim();
        }
        return null;
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());

        return values.toArray(new String[0]);
    }
}

