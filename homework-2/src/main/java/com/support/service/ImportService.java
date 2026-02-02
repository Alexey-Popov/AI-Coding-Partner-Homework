package com.support.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.support.dto.ImportResult;
import com.support.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ImportService {
    
    private final TicketService ticketService;
    
    public ImportService(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    
    public ImportResult importFile(MultipartFile file, String format) throws IOException {
        ImportResult.ImportResultBuilder resultBuilder = ImportResult.builder()
            .totalRecords(0)
            .successfulRecords(0)
            .failedRecords(0)
            .errors(new ArrayList<>());
        
        try {
            List<Map<String, String>> records = parseFile(file, format);
            resultBuilder.totalRecords(records.size());
            
            for (int i = 0; i < records.size(); i++) {
                try {
                    Ticket ticket = mapToTicket(records.get(i));
                    ticketService.createTicket(ticket);
                    resultBuilder.successfulRecords(resultBuilder.build().getSuccessfulRecords() + 1);
                } catch (Exception e) {
                    resultBuilder.failedRecords(resultBuilder.build().getFailedRecords() + 1);
                    resultBuilder.errors(addError(resultBuilder.build().getErrors(), 
                        i + 1, records.get(i).toString(), e.getMessage()));
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse file: " + e.getMessage(), e);
        }
        
        return resultBuilder.build();
    }
    
    private List<Map<String, String>> parseFile(MultipartFile file, String format) throws IOException {
        return switch (format.toLowerCase()) {
            case "csv" -> parseCsv(file);
            case "json" -> parseJson(file);
            case "xml" -> parseXml(file);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }
    
    private List<Map<String, String>> parseCsv(MultipartFile file) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> lines;
            try {
                lines = reader.readAll();
            } catch (Exception e) {
                throw new IOException("Failed to parse CSV file: " + e.getMessage(), e);
            }
            if (lines.isEmpty()) return records;
            
            String[] headers = lines.get(0);
            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i);
                Map<String, String> record = new HashMap<>();
                for (int j = 0; j < headers.length && j < values.length; j++) {
                    record.put(headers[j].trim(), values[j].trim());
                }
                records.add(record);
            }
        } catch (Exception e) {
            throw new IOException("CSV parsing error: " + e.getMessage(), e);
        }
        
        return records;
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseJson(MultipartFile file) throws IOException {
        com.fasterxml.jackson.databind.ObjectMapper mapper = 
            new com.fasterxml.jackson.databind.ObjectMapper();
        
        Object parsed = mapper.readValue(file.getInputStream(), Object.class);
        
        if (parsed instanceof List) {
            return (List<Map<String, String>>) ((List<?>) parsed).stream()
                .map(obj -> convertToStringMap((Map<?, ?>) obj))
                .toList();
        } else if (parsed instanceof Map) {
            return List.of(convertToStringMap((Map<?, ?>) parsed));
        }
        
        throw new IOException("Invalid JSON format");
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseXml(MultipartFile file) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        
        try {
            Map<String, Object> parsed = xmlMapper.readValue(
                file.getInputStream(), Map.class);
            
            Object tickets = parsed.get("ticket");
            if (tickets == null) {
                tickets = parsed.get("tickets");
            }
            
            if (tickets instanceof List) {
                return ((List<?>) tickets).stream()
                    .map(obj -> convertToStringMap((Map<?, ?>) obj))
                    .toList();
            } else if (tickets instanceof Map) {
                return List.of(convertToStringMap((Map<?, ?>) tickets));
            }
            
            return List.of(convertToStringMap(parsed));
        } catch (Exception e) {
            throw new IOException("XML parsing error: " + e.getMessage(), e);
        }
    }
    
    private Map<String, String> convertToStringMap(Map<?, ?> map) {
        Map<String, String> result = new HashMap<>();
        map.forEach((key, value) -> 
            result.put(String.valueOf(key), value != null ? String.valueOf(value) : null));
        return result;
    }
    
    private Ticket mapToTicket(Map<String, String> data) {
        Ticket ticket = Ticket.builder()
            .id(data.get("id"))
            .customerId(data.get("customer_id"))
            .customerEmail(data.get("customer_email"))
            .customerName(data.get("customer_name"))
            .subject(data.get("subject"))
            .description(data.get("description"))
            .assignedTo(data.get("assigned_to"))
            .build();
        
        // Parse enums
        if (data.containsKey("category")) {
            ticket.setCategory(Ticket.Category.valueOf(data.get("category").toUpperCase()));
        }
        if (data.containsKey("priority")) {
            ticket.setPriority(Ticket.Priority.valueOf(data.get("priority").toUpperCase()));
        }
        if (data.containsKey("status")) {
            ticket.setStatus(Ticket.Status.valueOf(data.get("status").toUpperCase()));
        }
        
        // Parse tags
        if (data.containsKey("tags")) {
            String tagsStr = data.get("tags");
            if (tagsStr != null && !tagsStr.isEmpty()) {
                ticket.setTags(Arrays.asList(tagsStr.split(",")));
            }
        }
        
        // Parse metadata
        Ticket.Metadata metadata = Ticket.Metadata.builder().build();
        if (data.containsKey("source")) {
            metadata.setSource(Ticket.Metadata.Source.valueOf(data.get("source").toUpperCase()));
        }
        if (data.containsKey("browser")) {
            metadata.setBrowser(data.get("browser"));
        }
        if (data.containsKey("device_type")) {
            metadata.setDeviceType(Ticket.Metadata.DeviceType.valueOf(
                data.get("device_type").toUpperCase()));
        }
        ticket.setMetadata(metadata);
        
        return ticket;
    }
    
    private List<ImportResult.ImportError> addError(List<ImportResult.ImportError> errors, 
                                                      int line, String data, String message) {
        List<ImportResult.ImportError> newErrors = new ArrayList<>(errors);
        newErrors.add(ImportResult.ImportError.builder()
            .lineNumber(line)
            .recordData(data.length() > 100 ? data.substring(0, 100) + "..." : data)
            .errorMessage(message)
            .build());
        return newErrors;
    }
}
