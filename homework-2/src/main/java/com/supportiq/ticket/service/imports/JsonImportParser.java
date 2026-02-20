package com.supportiq.ticket.service.imports;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.exception.MalformedImportFileException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class JsonImportParser {

    private final ObjectMapper objectMapper;

    public JsonImportParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<CreateTicketRequest> parse(InputStream inputStream) {
        try {
            return objectMapper.readValue(inputStream, new TypeReference<>() {});
        } catch (IOException e) {
            throw new MalformedImportFileException("Failed to parse JSON file: " + e.getMessage());
        }
    }
}
