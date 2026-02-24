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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class JsonImportService {

    private final TicketService ticketService;
    private final TicketValidationService validationService;
    private final ObjectMapper objectMapper;

    public JsonImportService(TicketService ticketService, TicketValidationService validationService) {
        this.ticketService = ticketService;
        this.validationService = validationService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
    }

    public ImportResult importFromJson(MultipartFile file) {
        ImportResult result = new ImportResult();

        try {
            List<JsonTicketData> ticketDataList = objectMapper.readValue(
                    file.getInputStream(), new TypeReference<List<JsonTicketData>>() {});

            result.setTotalRecords(ticketDataList.size());

            int recordNumber = 0;
            for (JsonTicketData data : ticketDataList) {
                recordNumber++;
                try {
                    Ticket ticket = convertToTicket(data);
                    Map<String, String> errors = validationService.validateTicketData(ticket);

                    if (errors.isEmpty()) {
                        Ticket saved = ticketService.saveTicket(ticket);
                        result.addImportedTicketId(saved.getId().toString());
                        result.setSuccessfulRecords(result.getSuccessfulRecords() + 1);
                    } else {
                        for (Map.Entry<String, String> error : errors.entrySet()) {
                            result.addError(new ImportResult.ImportError(
                                    recordNumber, error.getKey(), error.getValue(),
                                    objectMapper.writeValueAsString(data)));
                        }
                        result.setFailedRecords(result.getFailedRecords() + 1);
                    }
                } catch (Exception e) {
                    result.addError(new ImportResult.ImportError(
                            recordNumber, "parsing", "Failed to parse record: " + e.getMessage(),
                            data != null ? objectMapper.writeValueAsString(data) : "null"));
                    result.setFailedRecords(result.getFailedRecords() + 1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON file: " + e.getMessage(), e);
        }

        return result;
    }

    private Ticket convertToTicket(JsonTicketData data) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId(data.customerId);
        ticket.setCustomerEmail(data.customerEmail);
        ticket.setCustomerName(data.customerName);
        ticket.setSubject(data.subject);
        ticket.setDescription(data.description);

        if (data.category != null) {
            try {
                ticket.setCategory(TicketCategory.valueOf(data.category.toUpperCase()));
            } catch (IllegalArgumentException e) {
                ticket.setCategory(TicketCategory.OTHER);
            }
        } else {
            ticket.setCategory(TicketCategory.OTHER);
        }

        if (data.priority != null) {
            try {
                ticket.setPriority(TicketPriority.valueOf(data.priority.toUpperCase()));
            } catch (IllegalArgumentException e) {
                ticket.setPriority(TicketPriority.MEDIUM);
            }
        } else {
            ticket.setPriority(TicketPriority.MEDIUM);
        }

        if (data.status != null) {
            try {
                ticket.setStatus(TicketStatus.valueOf(data.status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                ticket.setStatus(TicketStatus.NEW);
            }
        } else {
            ticket.setStatus(TicketStatus.NEW);
        }

        ticket.setAssignedTo(data.assignedTo);
        ticket.setTags(data.tags);

        if (data.metadata != null) {
            TicketMetadata metadata = new TicketMetadata();
            if (data.metadata.source != null) {
                try {
                    metadata.setSource(TicketSource.valueOf(data.metadata.source.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    metadata.setSource(TicketSource.API);
                }
            }
            metadata.setBrowser(data.metadata.browser);
            if (data.metadata.deviceType != null) {
                try {
                    metadata.setDeviceType(DeviceType.valueOf(data.metadata.deviceType.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    metadata.setDeviceType(DeviceType.DESKTOP);
                }
            }
            ticket.setMetadata(metadata);
        }

        return ticket;
    }

    // Inner class to handle JSON deserialization with snake_case
    public static class JsonTicketData {
        public String customerId;
        public String customerEmail;
        public String customerName;
        public String subject;
        public String description;
        public String category;
        public String priority;
        public String status;
        public String assignedTo;
        public List<String> tags;
        public JsonMetadata metadata;

        public static class JsonMetadata {
            public String source;
            public String browser;
            public String deviceType;
        }
    }
}

