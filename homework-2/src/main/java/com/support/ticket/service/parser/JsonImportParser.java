package com.support.ticket.service.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.ticket.dto.CreateTicketRequest;
import com.support.ticket.exception.ImportException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Component
public class JsonImportParser {

    private final ObjectMapper objectMapper;

    public JsonImportParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<CreateTicketRequest> parse(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0) {
                return Collections.emptyList();
            }
            String content = new String(bytes).trim();
            if (content.startsWith("[")) {
                return objectMapper.readValue(content, new TypeReference<List<CreateTicketRequest>>() {});
            } else if (content.startsWith("{")) {
                CreateTicketRequest single = objectMapper.readValue(content, CreateTicketRequest.class);
                return List.of(single);
            } else {
                throw new ImportException("Invalid JSON format: expected array or object");
            }
        } catch (IOException e) {
            throw new ImportException("Failed to parse JSON file: " + e.getMessage(), e);
        }
    }
}
