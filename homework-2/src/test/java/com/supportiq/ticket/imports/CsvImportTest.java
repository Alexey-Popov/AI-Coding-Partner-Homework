package com.supportiq.ticket.imports;

import com.supportiq.ticket.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CsvImportTest extends BaseIntegrationTest {

    @Test
    void importValidCsv_allRecordsSucceed() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/valid_tickets.csv", "text/csv");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total_records").value(5))
                .andExpect(jsonPath("$.successful").value(5))
                .andExpect(jsonPath("$.failed").value(0))
                .andExpect(jsonPath("$.created_tickets", hasSize(5)));
    }

    @Test
    void importInvalidCsv_allRecordsFail() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/invalid_tickets.csv", "text/csv");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total_records").value(4))
                .andExpect(jsonPath("$.failed").value(greaterThan(0)))
                .andExpect(jsonPath("$.failures").isArray());
    }

    @Test
    void importMalformedCsv_returns400() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/malformed.csv", "text/csv");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importMixedCsv_partialSuccess() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/mixed_valid_invalid.csv", "text/csv");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successful").value(greaterThan(0)))
                .andExpect(jsonPath("$.failed").value(greaterThan(0)));
    }

    @Test
    void importEmptyCsv_zeroRecords() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/empty.csv", "text/csv");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total_records").value(0))
                .andExpect(jsonPath("$.successful").value(0));
    }

    @Test
    void importCsvWithAutoClassify_ticketsGetClassified() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/valid_tickets.csv", "text/csv");

        mockMvc.perform(multipart("/api/tickets/import")
                        .file(file)
                        .param("autoClassify", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successful").value(5));
    }

    private MockMultipartFile loadFixture(String path, String contentType) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        return new MockMultipartFile("file", resource.getFilename(), contentType, resource.getInputStream());
    }
}
