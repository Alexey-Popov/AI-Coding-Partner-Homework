package com.supportiq.ticket.imports;

import com.supportiq.ticket.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JsonImportTest extends BaseIntegrationTest {

    @Test
    void importValidJson_allRecordsSucceed() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/valid_tickets.json", "application/json");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total_records").value(5))
                .andExpect(jsonPath("$.successful").value(5))
                .andExpect(jsonPath("$.failed").value(0));
    }

    @Test
    void importInvalidJson_recordsFail() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/invalid_tickets.json", "application/json");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.failed").value(greaterThan(0)));
    }

    @Test
    void importMalformedJson_returns400() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/malformed.json", "application/json");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importMixedJson_partialSuccess() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/mixed_valid_invalid.json", "application/json");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successful").value(greaterThan(0)))
                .andExpect(jsonPath("$.failed").value(greaterThan(0)));
    }

    @Test
    void importEmptyJson_zeroRecords() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/empty.json", "application/json");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total_records").value(0));
    }

    private MockMultipartFile loadFixture(String path, String contentType) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        return new MockMultipartFile("file", resource.getFilename(), contentType, resource.getInputStream());
    }
}
