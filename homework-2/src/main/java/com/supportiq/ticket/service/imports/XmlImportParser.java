package com.supportiq.ticket.service.imports;

import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.enums.*;
import com.supportiq.ticket.exception.MalformedImportFileException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Component;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.*;

@Component
public class XmlImportParser {

    public List<CreateTicketRequest> parse(InputStream inputStream) {
        try {
            JAXBContext context = JAXBContext.newInstance(XmlTicketsWrapper.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Prevent XXE attacks by disabling external entities
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            SAXSource source = new SAXSource(xmlReader, new InputSource(inputStream));

            XmlTicketsWrapper wrapper = (XmlTicketsWrapper) unmarshaller.unmarshal(source);

            if (wrapper.getTickets() == null) {
                return List.of();
            }

            return wrapper.getTickets().stream()
                    .map(this::mapRecord)
                    .toList();
        } catch (JAXBException e) {
            throw new MalformedImportFileException("Failed to parse XML file: " + e.getMessage());
        } catch (Exception e) {
            throw new MalformedImportFileException("Failed to parse XML file: " + e.getMessage());
        }
    }

    private CreateTicketRequest mapRecord(XmlTicketRecord record) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId(record.getCustomerId());
        request.setCustomerName(record.getCustomerName());
        request.setCustomerEmail(record.getCustomerEmail());
        request.setSubject(record.getSubject());
        request.setDescription(record.getDescription());

        if (record.getCategory() != null && !record.getCategory().isBlank()) {
            request.setCategory(TicketCategory.fromValue(record.getCategory()));
        }
        if (record.getPriority() != null && !record.getPriority().isBlank()) {
            request.setPriority(TicketPriority.fromValue(record.getPriority()));
        }
        if (record.getSource() != null && !record.getSource().isBlank()) {
            request.setSource(Source.fromValue(record.getSource()));
        }
        request.setBrowser(record.getBrowser());
        if (record.getDeviceType() != null && !record.getDeviceType().isBlank()) {
            request.setDeviceType(DeviceType.fromValue(record.getDeviceType()));
        }
        if (record.getTags() != null && !record.getTags().isBlank()) {
            request.setTags(new HashSet<>(Arrays.asList(record.getTags().split(";"))));
        }

        return request;
    }
}
