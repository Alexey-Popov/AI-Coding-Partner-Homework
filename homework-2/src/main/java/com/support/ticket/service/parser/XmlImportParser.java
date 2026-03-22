package com.support.ticket.service.parser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.support.ticket.dto.CreateTicketRequest;
import com.support.ticket.exception.ImportException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Component
public class XmlImportParser {

    private final XmlMapper xmlMapper;

    public XmlImportParser() {
        this.xmlMapper = new XmlMapper();
    }

    public List<CreateTicketRequest> parse(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0) {
                return Collections.emptyList();
            }
            String content = new String(bytes).trim();
            if (content.isEmpty()) {
                return Collections.emptyList();
            }
            TicketList ticketList = xmlMapper.readValue(content, TicketList.class);
            if (ticketList.getTickets() == null) {
                return Collections.emptyList();
            }
            return ticketList.getTickets();
        } catch (IOException e) {
            throw new ImportException("Failed to parse XML file: " + e.getMessage(), e);
        }
    }

    @JacksonXmlRootElement(localName = "tickets")
    public static class TicketList {

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "ticket")
        private List<CreateTicketRequest> tickets;

        public List<CreateTicketRequest> getTickets() { return tickets; }
        public void setTickets(List<CreateTicketRequest> tickets) { this.tickets = tickets; }
    }
}
