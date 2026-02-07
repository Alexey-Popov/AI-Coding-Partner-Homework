package com.supportiq.ticket.imports;

import com.supportiq.ticket.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class XmlImportTest extends BaseIntegrationTest {

    @Test
    void importValidXml_allRecordsSucceed() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/valid_tickets.xml", "application/xml");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total_records").value(5))
                .andExpect(jsonPath("$.successful").value(5))
                .andExpect(jsonPath("$.failed").value(0));
    }

    @Test
    void importInvalidXml_recordsFail() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/invalid_tickets.xml", "application/xml");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.failed").value(greaterThan(0)));
    }

    @Test
    void importMalformedXml_returns400() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/malformed.xml", "application/xml");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importMixedXml_partialSuccess() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/mixed_valid_invalid.xml", "application/xml");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successful").value(greaterThan(0)))
                .andExpect(jsonPath("$.failed").value(greaterThan(0)));
    }

    @Test
    void importEmptyXml_zeroRecords() throws Exception {
        MockMultipartFile file = loadFixture("fixtures/empty.xml", "application/xml");

        mockMvc.perform(multipart("/api/tickets/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total_records").value(0));
    }

    private MockMultipartFile loadFixture(String path, String contentType) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        return new MockMultipartFile("file", resource.getFilename(), contentType, resource.getInputStream());
    }
}
