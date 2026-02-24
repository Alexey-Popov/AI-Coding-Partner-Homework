package com.css.service.importer;

import com.css.dto.ImportResult;
import com.css.exception.ImportException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketImportServiceTest {

    @Mock
    private CsvImportService csvImportService;

    @Mock
    private JsonImportService jsonImportService;

    @Mock
    private XmlImportService xmlImportService;

    @InjectMocks
    private TicketImportService ticketImportService;

    // -------------------------------------------------------------------------
    // Routing by extension
    // -------------------------------------------------------------------------

    @Test
    void importTickets_csvFile_delegatesToCsvService() {
        ImportResult expected = new ImportResult();
        expected.setSuccessfulRecords(3);
        when(csvImportService.importFromCsv(any())).thenReturn(expected);

        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets.csv", "text/csv", "header\nvalue".getBytes());

        ImportResult result = ticketImportService.importTickets(file);

        assertThat(result).isSameAs(expected);
        verify(csvImportService).importFromCsv(file);
        verifyNoInteractions(jsonImportService, xmlImportService);
    }

    @Test
    void importTickets_jsonFile_delegatesToJsonService() {
        ImportResult expected = new ImportResult();
        when(jsonImportService.importFromJson(any())).thenReturn(expected);

        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets.json", "application/json", "[]".getBytes());

        ImportResult result = ticketImportService.importTickets(file);

        assertThat(result).isSameAs(expected);
        verify(jsonImportService).importFromJson(file);
        verifyNoInteractions(csvImportService, xmlImportService);
    }

    @Test
    void importTickets_xmlFile_delegatesToXmlService() {
        ImportResult expected = new ImportResult();
        when(xmlImportService.importFromXml(any())).thenReturn(expected);

        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets.xml", "application/xml", "<tickets/>".getBytes());

        ImportResult result = ticketImportService.importTickets(file);

        assertThat(result).isSameAs(expected);
        verify(xmlImportService).importFromXml(file);
        verifyNoInteractions(csvImportService, jsonImportService);
    }

    @Test
    void importTickets_csvExtensionCaseInsensitive_delegatesToCsvService() {
        when(csvImportService.importFromCsv(any())).thenReturn(new ImportResult());

        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets.CSV", "text/csv", "data".getBytes());

        ticketImportService.importTickets(file);

        verify(csvImportService).importFromCsv(file);
    }

    // -------------------------------------------------------------------------
    // Error cases
    // -------------------------------------------------------------------------

    @Test
    void importTickets_nullFilename_throwsImportException() {
        org.springframework.web.multipart.MultipartFile file =
                mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);

        assertThatThrownBy(() -> ticketImportService.importTickets(file))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining("File name is required");
    }

    @Test
    void importTickets_noExtension_throwsImportException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets", "text/plain", "data".getBytes());

        assertThatThrownBy(() -> ticketImportService.importTickets(file))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining("extension");
    }

    @Test
    void importTickets_unsupportedExtension_throwsImportException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets.xlsx", "application/vnd.ms-excel", "data".getBytes());

        assertThatThrownBy(() -> ticketImportService.importTickets(file))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining("xlsx")
                .hasMessageContaining("Unsupported file format");
    }

    @Test
    void importTickets_unsupportedFormat_mentionsSupportedFormats() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "tickets.txt", "text/plain", "data".getBytes());

        assertThatThrownBy(() -> ticketImportService.importTickets(file))
                .isInstanceOf(ImportException.class)
                .hasMessageContaining("CSV")
                .hasMessageContaining("JSON")
                .hasMessageContaining("XML");
    }
}
