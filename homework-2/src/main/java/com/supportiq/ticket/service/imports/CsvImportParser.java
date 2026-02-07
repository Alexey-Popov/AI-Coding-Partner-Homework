package com.supportiq.ticket.service.imports;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.enums.*;
import com.supportiq.ticket.exception.MalformedImportFileException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class CsvImportParser {

    private static final List<String> REQUIRED_HEADERS = List.of(
            "customer_name", "customer_email", "subject", "description"
    );

    public List<CreateTicketRequest> parse(InputStream inputStream) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String[]> rows = reader.readAll();

            if (rows.isEmpty()) {
                return List.of();
            }

            String[] headers = rows.getFirst();
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim().toLowerCase(), i);
            }

            for (String required : REQUIRED_HEADERS) {
                if (!headerMap.containsKey(required)) {
                    throw new MalformedImportFileException("Missing required CSV header: " + required);
                }
            }

            List<CreateTicketRequest> requests = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                requests.add(mapRow(row, headerMap));
            }

            return requests;
        } catch (IOException | CsvException e) {
            throw new MalformedImportFileException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    private CreateTicketRequest mapRow(String[] row, Map<String, Integer> headerMap) {
        CreateTicketRequest request = new CreateTicketRequest();

        request.setCustomerId(getField(row, headerMap, "customer_id"));
        request.setCustomerName(getField(row, headerMap, "customer_name"));
        request.setCustomerEmail(getField(row, headerMap, "customer_email"));
        request.setSubject(getField(row, headerMap, "subject"));
        request.setDescription(getField(row, headerMap, "description"));

        String category = getField(row, headerMap, "category");
        if (category != null && !category.isBlank()) {
            request.setCategory(TicketCategory.fromValue(category));
        }

        String priority = getField(row, headerMap, "priority");
        if (priority != null && !priority.isBlank()) {
            request.setPriority(TicketPriority.fromValue(priority));
        }

        String source = getField(row, headerMap, "source");
        if (source != null && !source.isBlank()) {
            request.setSource(Source.fromValue(source));
        }

        request.setBrowser(getField(row, headerMap, "browser"));

        String deviceType = getField(row, headerMap, "device_type");
        if (deviceType != null && !deviceType.isBlank()) {
            request.setDeviceType(DeviceType.fromValue(deviceType));
        }

        String tags = getField(row, headerMap, "tags");
        if (tags != null && !tags.isBlank()) {
            request.setTags(new HashSet<>(Arrays.asList(tags.split(";"))));
        }

        return request;
    }

    private String getField(String[] row, Map<String, Integer> headerMap, String field) {
        Integer index = headerMap.get(field);
        if (index == null || index >= row.length) return null;
        String value = row[index].trim();
        return value.isEmpty() ? null : value;
    }
}
