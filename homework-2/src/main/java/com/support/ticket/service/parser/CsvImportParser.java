package com.support.ticket.service.parser;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.support.ticket.dto.CreateTicketRequest;
import com.support.ticket.exception.ImportException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Component
public class CsvImportParser {

    private static final Set<String> REQUIRED_COLUMNS = Set.of(
            "customer_id", "customer_email", "customer_name", "subject", "description"
    );

    public List<CreateTicketRequest> parse(InputStream inputStream) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) {
                return Collections.emptyList();
            }

            String[] headers = allRows.get(0);
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim().toLowerCase(), i);
            }

            for (String required : REQUIRED_COLUMNS) {
                if (!headerMap.containsKey(required)) {
                    throw new ImportException("Missing required column: " + required);
                }
            }

            List<CreateTicketRequest> requests = new ArrayList<>();
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                if (row.length == 0 || (row.length == 1 && row[0].isBlank())) {
                    continue;
                }
                CreateTicketRequest request = new CreateTicketRequest();
                request.setCustomerId(getColumn(row, headerMap, "customer_id"));
                request.setCustomerEmail(getColumn(row, headerMap, "customer_email"));
                request.setCustomerName(getColumn(row, headerMap, "customer_name"));
                request.setSubject(getColumn(row, headerMap, "subject"));
                request.setDescription(getColumn(row, headerMap, "description"));
                requests.add(request);
            }

            return requests;
        } catch (IOException | CsvException e) {
            throw new ImportException("Failed to parse CSV file: " + e.getMessage(), e);
        }
    }

    private String getColumn(String[] row, Map<String, Integer> headerMap, String column) {
        Integer index = headerMap.get(column);
        if (index == null || index >= row.length) {
            return null;
        }
        return row[index].trim();
    }
}
